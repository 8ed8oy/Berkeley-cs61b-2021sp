package gitlet.logic;

import gitlet.Utils;
import gitlet.logic.WorkingTree;
import gitlet.models.Commit;
import gitlet.storage.Persistence;
import gitlet.storage.ReferenceStore;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Utils.*;

/**
 * Merge logic extraction from Repository.
 */
public class Merger {

    public static void merge(String targetBranch) {
        if (!Persistence.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        if (targetBranch == null || targetBranch.isEmpty()) {
            System.out.println("A branch name is required.");
            return;
        }
        File targetBranchFile = ReferenceStore.getBranchFile(targetBranch);
        if (!targetBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        String currentBranch = ReferenceStore.getCurrentBranchName();
        if (targetBranch.equals(currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        StagingArea stagingArea = StagingArea.fromFile();
        if (!stagingArea.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }

        Commit currentCommit = Persistence.readHeadCommit();
        String currentCommitID = sha1(serialize(currentCommit));

        Commit targetCommit = getCommit(targetBranch);
        String targetCommitID = sha1(serialize(targetCommit));

        Commit splitPointCommit = HistoryWalker.findSplitPoint(currentCommit, targetCommit);
        if (splitPointCommit == null) {
            System.out.println("Gitlet goes wrong.");
            return;
        }
        String splitCommitID = sha1(serialize(splitPointCommit));

        if (splitCommitID.equals(targetCommitID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitCommitID.equals(currentCommitID)) {
            checkoutBranch(targetBranch);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        boolean encounterMergeConflict = false;
        Map<String, String> splitBlobs = splitPointCommit.getBlobsID();
        Map<String, String> currentBlobs = currentCommit.getBlobsID();
        Map<String, String> targetBlobs = targetCommit.getBlobsID();
        Set<String> allFilesSet = new HashSet<>();
        allFilesSet.addAll(splitBlobs.keySet());
        allFilesSet.addAll(currentBlobs.keySet());
        allFilesSet.addAll(targetBlobs.keySet());

        for (String fileName : allFilesSet) {
            String s = splitBlobs.get(fileName);
            String c = currentBlobs.get(fileName);
            String t = targetBlobs.get(fileName);

            if (c != null && c.equals(s) && t != null && !t.equals(s)) {
                // case 1
                WorkingTree.checkoutFile(targetCommit, fileName);
                stagingArea.add(fileName);
            } else if (c != null && !c.equals(s) && t.equals(s)) {
                // case 2 do nothing
            } else if (c != null && c.equals(t)) {
                // case 3 do nothing
            } else if (s == null && t == null && c != null) {
                // case 4 do nothing
            } else if (s == null && c == null && t != null) {
                // case 5
                WorkingTree.checkoutFile(targetCommit, fileName);
                stagingArea.add(fileName);
            } else if (s != null && c != null && c.equals(s) && t == null) {
                // case 6
                stagingArea.rm(fileName);
            } else if (s != null && c == null && t != null && t.equals(s)) {
                // case 7
            }
            // conflict cases
            else if (isConflict(s, c, t)) {
                encounterMergeConflict = true;

                String conflictContent = "";
                conflictContent += "<<<<<<< HEAD\n";
                if (c != null) {
                    File cBlobFile = join(Persistence.BLOBS_DIR, c);
                    byte[] cContent = readContents(cBlobFile);
                    conflictContent += new String(cContent);
                }
                conflictContent += "=======\n";
                if (t != null) {
                    File tBlobFile = join(Persistence.BLOBS_DIR, t);
                    byte[] tContent = readContents(tBlobFile);
                    conflictContent += new String(tContent);
                }
                conflictContent += ">>>>>>>\n";

                File fileToWrite = join(Persistence.CWD, fileName);
                writeContents(fileToWrite, conflictContent);
                stagingArea.add(fileName);
            }
        }

        stagingArea = StagingArea.fromFile();

        Commit mergeCommit = new Commit(
                "Merged " + targetBranch + " into " + currentBranch + ".",
                currentCommitID,
                targetCommitID
        );

        Map<String, String> merged = new HashMap<>(currentBlobs);
        merged.putAll(stagingArea.getAddedFiles());
        for (String rmName : stagingArea.getRemovedFiles()) {
            merged.remove(rmName);
        }
        mergeCommit.setBlobsID(merged);
        Persistence.saveCommit(mergeCommit);

        String mergeCommitID = sha1(serialize(mergeCommit));
        ReferenceStore.updateHead(mergeCommitID);
        stagingArea.clear();

        writeMergeLogFixed(mergeCommitID, mergeCommit, currentCommitID, targetCommitID);

        if (encounterMergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private static Commit getCommit(String branchName) {
        String commitID = ReferenceStore.readBranchHead(branchName);
        if (commitID == null) return null;
        File commitFile = join(Persistence.COMMITS_DIR, commitID);
        return readObject(commitFile, Commit.class);
    }

    private static boolean checkoutBranch(String branchName) {
        File branchFile = ReferenceStore.getBranchFile(branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            return false;
        }
        String currentBranch = ReferenceStore.getCurrentBranchName();
        if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return false;
        }
        Commit commit = getCommit(branchName);

        if (!WorkingTree.checkoutTree(commit)) {
            return false;
        }

        ReferenceStore.setCurrentBranchName(branchName);
        StagingArea stagingArea = StagingArea.fromFile();
        stagingArea.clear();
        return true;
    }

    private static boolean isConflict(String s, String c, String t) {
        if (s == null) {
            return c != null && t != null && !c.equals(t);
        }
        if (c == null && t == null) return false;
        if (c == null) return t != null && !t.equals(s);
        if (t == null) return !c.equals(s);
        boolean cChanged = !c.equals(s);
        boolean tChanged = !t.equals(s);
        return cChanged && tChanged && !c.equals(t);
    }

    private static void writeMergeLogFixed(String commitID, Commit commit, String parent1, String parent2) {
        String date = commit.getTimestamp();
        String msg = commit.getMessage();
        String p1 = parent1;
        String p2 = parent2;
        String context = "===\n"
                + "commit " + commitID + "\n"
                + "Merge: " + p1 + " " + p2 + "\n"
                + "Date: " + date + "\n"
                + msg + "\n\n";
        String prev = readContentsAsString(Persistence.GLOBAL_LOG_FILE);
        writeContents(Persistence.GLOBAL_LOG_FILE, context + prev);
    }
}
