package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Fangheng Wang
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(GITLET_DIR, "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File INDEX_FILE = join(GITLET_DIR, "index");
    public static final File GLOBAL_LOG_FILE = join(GITLET_DIR, "global_log");


    public static void checkInitialized() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();

        /* create and save initial commit */
        Commit initialCommit = new Commit("initial commit", null);
        String commitID = Utils.sha1(serialize(initialCommit));
        File initialCommitFile = join(COMMITS_DIR, commitID);
        writeObject(initialCommitFile, initialCommit);

        /* create master branch pointing to initial commit */
        File masterBranch = join(HEADS_DIR, "master");
        writeContents(masterBranch, commitID);

        /* write initial log */
        writeCommitLog(commitID, initialCommit);

        /* set HEAD to master */
        writeContents(HEAD_FILE, "master");
    }

    /** Logic flow:
     * 1. Check if repository is initialized.
     * 2. Check if the file to be added exists.
     * 3. Load the current staging area from the index file.
     * 4. Add the file to the staging area.
     * 5. Save the updated staging area back to the index file.
     * @param fileName
     */
    public static void add(String fileName) {
        checkInitialized();
        File fileToAdd = join(CWD, fileName);

        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        StagingArea stagingArea = StagingArea.fromFile();
        stagingArea.add(fileName);
    }

    /** Logic flow:
     * 1. Check if repository is initialized.
     * 2. Check if the commit message is not null.
     * 3. Load staging area from the index file.
     * 4. If there are no changes staged for commit, give warning and return.
     * 5. Create a new commit object with the given message and the current head commit as its parent.
     * 6. Apply staging area changes to new commit's blobsID
     * 7. Point the current branch to the new commit.
     * 8. Clear the staging area.
     * 9. Write log:
     * ===
     * commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48
     * Date: Thu Nov 9 20:00:05 2017 -0800
     * A commit message.
     *
     * @param msg
     */
    public static void commit(String msg){
        checkInitialized();

        StagingArea stagingArea = StagingArea.fromFile();
        if(stagingArea.isEmpty()){
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit parentCommit = Utils.readObject(getHeadCommitFile(), Commit.class);
        String currentBranch = readContentsAsString(HEAD_FILE);
        File branchFile = join(HEADS_DIR, currentBranch);
        String parentID = readContentsAsString(branchFile);
        Commit newCommit = new Commit(msg, parentID);

        /* Inherit parentcommit's blobsID */
        Map<String, String> parentBlobsID = parentCommit.getBlobsID();
        HashMap<String, String> newBlobsID;
        if (parentBlobsID == null) {
            newBlobsID = new HashMap<>();
        } else {
            newBlobsID = new HashMap<>(parentBlobsID);
        }

        /* Apply staging area changes to new commit's blobsID */
        Map<String, String> addedFiles = stagingArea.getAddedFiles();
        newBlobsID.putAll(addedFiles);

        java.util.Set<String> removedFiles = stagingArea.getRemovedFiles();
        for (String fileName : removedFiles) {
            newBlobsID.remove(fileName);
        }

        newCommit.setBlobsID(newBlobsID);
        newCommit.save();

        String newCommitID = Utils.sha1(Utils.serialize(newCommit));
        updateHead(newCommitID);

        stagingArea.clear();

        writeCommitLog(newCommitID, newCommit);
    }

    /** Logic flow:
     * 1. Check if repository is initialized.
     * 2. Load the current staging area from the index file.
     * 3. Check if the file is staged for addition or tracked in the current commit.
     *    - If both not, print warning and return.
     *    - If staged for addition, unstage it.
     *    - If tracked in current commit, mark it for removal and delete from working directory.
     4. Save the updated staging area back to the index file.
     * @param fileName
     */
    public static void rm(String fileName) {
        checkInitialized();

        StagingArea stagingArea = StagingArea.fromFile();
        Commit headCommit = Utils.readObject(getHeadCommitFile(), Commit.class);
        boolean isStaged = stagingArea.getAddedFiles().containsKey(fileName);
        boolean isTracked = headCommit.getBlobsID().containsKey(fileName);

        if (!isStaged && !isTracked) {
            System.out.println("No reason to remove the file.");
            return;
        }

        if (isStaged) {
            stagingArea.getAddedFiles().remove(fileName);
        } else if (isTracked) {
            stagingArea.getRemovedFiles().add(fileName);
            File fileToDelete = join(CWD, fileName);
            if (fileToDelete.exists()) {
                Utils.restrictedDelete(fileToDelete);
            }
        }
        stagingArea.save();
    }

    public static void log(){
        checkInitialized();
        File currentCommitFile = getHeadCommitFile();
        while (currentCommitFile != null) {
            Commit currentCommit = readObject(currentCommitFile, Commit.class);
            String commitID = sha1(serialize(currentCommit));
            String Date = currentCommit.getTimestamp();
            String message = currentCommit.getMessage();
            String context = commitLogcontext(commitID, Date, message);
            System.out.print(context);
            String parentID = currentCommit.getParent();
            if (parentID == null) {
                break;
            }
            currentCommitFile = join(COMMITS_DIR, parentID);
        }
    }

    /** Global log is stored in GLOBAL_LOG_FILE, which is different from the doc. */
    public static void globalLog(){
        checkInitialized();
        String globalLog = readContentsAsString(GLOBAL_LOG_FILE);
        System.out.print(globalLog);
    }

    public static void find(String message){
        checkInitialized();
        List<String> commitIDs = Utils.plainFilenamesIn(COMMITS_DIR);
        for(String commitID : commitIDs){
            File commitFile = join(COMMITS_DIR, commitID);
            Commit commit = readObject(commitFile, Commit.class);
            if(commit.getMessage().equals(message)){
                System.out.println(commitID);
            }
        }
    }

    /**
     * 1. Check if repository is initialized.
     * 2. Print out the status of
     * branches, * current branch marked with a *,
     * staged files,
     * removed files,
     * modifications not staged for commit,
     * untracked files.
     */
    public static void status(){
        checkInitialized();
        StagingArea stagingArea = StagingArea.fromFile();
        List<String> allFiles = listWorkingFiles();
        Set<String> allFilesSet = new HashSet<>(allFiles);
        Map<String, String> addedFiles = stagingArea.getAddedFiles();
        Set<String> removedFiles = stagingArea.getRemovedFiles();
        /** get blobsmap from headcommit. */
        Commit headCommit = Utils.readObject(getHeadCommitFile(), Commit.class);
        Map<String, String> headBlobs = headCommit.getBlobsID();
        if (headBlobs == null) {
            headBlobs = new HashMap<>();
        }

        Map<String, String> workingBlobs = new HashMap<>();
        for (String fileName : allFiles) {
            File file = join(CWD, fileName);
            byte[] content = readContents(file);
            workingBlobs.put(fileName, Utils.sha1((Object) content));
        }

        String currentBranch = readContentsAsString(HEAD_FILE);
        List<String> branchNames = Utils.plainFilenamesIn(HEADS_DIR);
        Collections.sort(branchNames);

        System.out.println("=== Branches ===");
        for (String branchName : branchNames) {
            if (branchName.equals(currentBranch)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println("\n");

        System.out.println("=== Staged Files ===");
        List<String> stagedList = new ArrayList<>(addedFiles.keySet());
        Collections.sort(stagedList);
        for (String fileName : stagedList) {
            System.out.println(fileName);
        }
        System.out.println("\n");

        System.out.println("=== Removed Files ===");
        List<String> removedList = new ArrayList<>(removedFiles);
        Collections.sort(removedList);
        for (String fileName : removedList) {
            System.out.println(fileName);
        }
        System.out.println("\n");

        /**
         * case:
         * 1. Tracked in the current commit, changed in the working directory, but not staged.
         * 2. Staged for addition, but with different contents than in the working directory
         * 3. Staged for addition, but deleted in the working directory
         * 4. Not staged for removal, but tracked in the current commit
         *          and deleted from the working directory.
         */
        System.out.println("=== Modifications Not Staged For Commit ===");
        Map<String, String> modifications = new TreeMap<>();
        /** case 1. */
        for (String fileName : allFilesSet) {
            if (!addedFiles.containsKey(fileName) && headBlobs.containsKey(fileName)) {
                String blobsID = workingBlobs.get(fileName);
                if (blobsID != null && !headBlobs.get(fileName).equals(blobsID)) {
                    modifications.put(fileName, "modified");
                }
            }
        }
        /** case 2 and 3. */
        for (String addedFile: addedFiles.keySet()){
            if (allFilesSet.contains(addedFile)) {
                String blobsID = workingBlobs.get(addedFile);
                if (blobsID != null && !addedFiles.get(addedFile).equals(blobsID)) {
                    modifications.put(addedFile, "modified");
                }
            } else {
                modifications.put(addedFile, "deleted");
            }
        }
        /** case 4. */
        for (String fileName : headBlobs.keySet()){
            if (!allFilesSet.contains(fileName)
                    && !removedFiles.contains(fileName)){
                modifications.put(fileName, "deleted");
            }
        }
        for (Map.Entry<String, String> entry : modifications.entrySet()) {
            System.out.println(entry.getKey() + " (" + entry.getValue() + ")");
        }
        System.out.println("\n");

        System.out.println("=== Untracked Files ===");
        List<String> untracked = new ArrayList<>();
        for (String fileName : allFilesSet) {
            if(!addedFiles.containsKey(fileName)
                    && !headBlobs.containsKey(fileName)
                    && !removedFiles.contains(fileName)){
                untracked.add(fileName);
            }
        }
        Collections.sort(untracked);
        for (String fileName : untracked) {
            System.out.println(fileName);
        }
        System.out.println("\n");
    }

    public static void checkoutFile(String fileName){
        checkInitialized();
        Commit headCommit = Utils.readObject(getHeadCommitFile(), Commit.class);
        backupFile(headCommit, fileName);
    }

    /**
     * Find commitID globally in COMMITS_DIR.
     * @param shortCommitID
     * @param fileName
     */
    public static void checkoutFileFromCommit(String shortCommitID, String fileName){
        checkInitialized();
        if (shortCommitID == null || shortCommitID.isEmpty()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        List<String> commitIDs = Utils.plainFilenamesIn(COMMITS_DIR);

        String findCommitID = getFullCommitID(shortCommitID, commitIDs);
        if (findCommitID == null) return;
        File commitFile = join(COMMITS_DIR, findCommitID);
        Commit findCommit = Utils.readObject(commitFile, Commit.class);
        backupFile(findCommit, fileName);
    }

    /**
     * Logic flow:
     * 1. Check if repository is initialized.
     * 2. - If the branch with the given name exists in HEADS_DIR,
     *      print "Branch do not exist." and return.
     *    - If it is the current branch,
     *      print "No need to checkout the current branch." and return.
     *    - If a working file is untracked in the current branch and would be overwritten by the checkout,
     *      print There is an untracked file in the way; delete it, or add and commit it first.
     * 4. Update HEAD_FILE to point to the new branch name.
     * 5. Backup all files from the commit that the new branch points to.
     * 6. Clear the staging area.
     * @param branchName
     */
    public static void checkoutBranch(String branchName){
        checkInitialized();
        File branchFile = join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        String currentBranch = readContentsAsString(HEAD_FILE);
        if (currentBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit commit = getCommit(branchName);

        if (!checkoutTree(commit)) {
            return;
        }

        writeContents(HEAD_FILE, branchName);
        StagingArea stagingArea = StagingArea.fromFile();
        stagingArea.clear();
    }

    public static void branch(String branchName){
        checkInitialized();
        if (branchName == null || branchName.isEmpty()) {
            System.out.println("Branch name cannot be empty.");
            return;
        }
        File branchFile = join(HEADS_DIR, branchName);
        if (branchFile.exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        File headCommitFile = getHeadCommitFile();
        String headCommitID = readContentsAsString(headCommitFile);
        writeContents(branchFile, headCommitID);
    }

    public static void rmBranch(String branchName){
        checkInitialized();
        if (branchName == null || branchName.isEmpty()) {
            System.out.println("Branch name cannot be empty.");
            return;
        }
        File branchFile = join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String currentBranch = readContentsAsString(HEAD_FILE);
        if (currentBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        Utils.restrictedDelete(branchFile);
    }

    public static void reSet(String commitIDToReset) {
        checkInitialized();
        if (commitIDToReset == null || commitIDToReset.isEmpty()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        List<String> commitIDs = Utils.plainFilenamesIn(COMMITS_DIR);
        String findCommitID = getFullCommitID(commitIDToReset, commitIDs);
        if (findCommitID == null) return;

        File commitFile = join(COMMITS_DIR, findCommitID);
        Commit targetCommit = Utils.readObject(commitFile, Commit.class);

        if (!checkoutTree(targetCommit)) {
            return;
        }

        String currentBranch = readContentsAsString(HEAD_FILE);
        File branchHeadFile = join(HEADS_DIR, currentBranch);
        writeContents(branchHeadFile, findCommitID);

        StagingArea stagingArea = StagingArea.fromFile();
        stagingArea.clear();
    }

    /**
     * Logic flow:
     * 1. Check if repository is initialized and check input.
     * 2. If the branch with the given name does not exist in HEADS_DIR,
     *      print "A branch with that name does not exist." and return.
     * 3. If given branch is ancestor(merge a file before me or the file is same),
     *      print "Given branch is an ancestor of the current branch." and return.
     * 4. If the branch is "Fast-forward" (current branch is ancestor of given branch),
     *      'checkoutBranch(branchName)' and print "Current branch fast-forwarded." and return.
     * 5. Else, this is a true merge.
     * @param targetBranch
     */
    public static void merge(String targetBranch){
        checkInitialized();
        if (targetBranch == null || targetBranch.isEmpty()) {
            System.out.println("A branch name is required.");
            return;
        }
        File targetBranchFile = join(HEADS_DIR, targetBranch);;
        if (!targetBranchFile.exists()) {
            System.out.println("A branch with that name does not exist.");
            return;
        }

        String currentBranch = readContentsAsString(HEAD_FILE);
        if (targetBranch.equals(readContentsAsString(HEAD_FILE))) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        StagingArea stagingArea = StagingArea.fromFile();
        if (!stagingArea.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }

        File currentHeadFile = getHeadCommitFile();
        Commit currentCommit = readObject(currentHeadFile, Commit.class);
        String currentCommitID = sha1(serialize(currentCommit));

        Commit targetCommit = getCommit(targetBranch);
        String targetCommitID = sha1(serialize(targetCommit));

        Commit splitPointCommit = findSplitPoint(currentCommit, targetCommit);
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

        /** True merge logic flow:
         * s:file in splitPointCommit
         * c:file in currentCommit
         * t:file in targetCommit
         * 0:file not in commit
         *
         * 1. c==s && t!=s && t!=0: checkout t, add t
         * 2. c!=s && t==s: do nothing
         * 3. c==t: do nothing
         *  - If a file was removed from both the current and given branch,
         *    but a file of the same name is present in the working directory,
         *    it is left alone and continues to be not tracked nor staged in the merge.
         * 4. s == 0 == t && c != 0: do nothing
         * 5. s == 0 && c == 0 && t != 0: checkout t, add t
         * 6. s != 0 && c == s && t == 0: rm c
         * 7. s != 0 && c == 0 && t == s: do nothing
         * 8.conflict cases
         */
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
                backupFile(targetCommit, fileName);
                add(fileName);
            } else if (c != null && !c.equals(s) && t.equals(s)) {
                // case 2 do nothing
            } else if (c != null && c.equals(t)) {
                // case 3 do nothing
            } else if (s == null && t == null && c != null) {
                // case 4 do nothing
            } else if (s == null && c == null && t != null) {
                // case 5
                backupFile(targetCommit, fileName);
                add(fileName);
            } else if (s != null && c != null && c.equals(s) && t == null) {
                // case 6
                rm(fileName);
            } else if (s != null && c == null && t != null && t.equals(s)) {
                // case 7
            }
            /** case 8 conflict
             *   - s != 0 && c != s && t != s && c != t
             *   - s != 0 && (c != s && t == 0) or (t != s && c == 0)
             *   - s == 0 && c != 0 && t != 0 && c != t
             *
             * conflict format:
             * <<<<<<< HEAD
             * (contents of file in current branch)
             * =======
             * (contents of file in target branch)
             * >>>>>>>
             */

            else if (isConflict(s, c, t)) {
                encounterMergeConflict = true;

                String conflictContent = "";
                conflictContent += "<<<<<<< HEAD\n";
                if (c != null) {
                    File cBlobFile = join(BLOBS_DIR, c);
                    byte[] cContent = readContents(cBlobFile);
                    conflictContent += new String(cContent);
                }
                conflictContent += "=======\n";
                if (t != null) {
                    File tBlobFile = join(BLOBS_DIR, t);
                    byte[] tContent = readContents(tBlobFile);
                    conflictContent += new String(tContent);
                }
                conflictContent += ">>>>>>>\n";

                File fileToWrite = join(CWD, fileName);
                writeContents(fileToWrite, conflictContent);
                add(fileName);
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
        mergeCommit.save();

        String mergeCommitID = sha1(serialize(mergeCommit));
        updateHead(mergeCommitID);
        stagingArea.clear();

        writeMergeLogFixed(mergeCommitID, mergeCommit, currentCommitID, targetCommitID);

        if (encounterMergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /*****************************************************************************************/
    /**                               Helper methods.                                        */
    /*****************************************************************************************/

    /** @return File object of the head commit.
     */
    public static File getHeadCommitFile() {
        String currentBranch = readContentsAsString(HEAD_FILE);
        File branchFile = join(HEADS_DIR, currentBranch);
        String headCommitID = readContentsAsString(branchFile);
        return join(COMMITS_DIR, headCommitID);
    }

    /** Update the current branch to point to the new commit ID.
     * @param newCommitID
     */
    private static void updateHead(String newCommitID) {
        String currentBranch = readContentsAsString(HEAD_FILE);
        File branchFile = join(HEADS_DIR, currentBranch);
        writeContents(branchFile, newCommitID);
    }

    private static void writeCommitLog(String commitID, Commit commit ){
        String Date = commit.getTimestamp();
        String message = commit.getMessage();
        String context = commitLogcontext(commitID, Date, message);
        String previousLog = "";
        if (GLOBAL_LOG_FILE.exists()) {
            previousLog = readContentsAsString(GLOBAL_LOG_FILE);
        }
        String newLog = context + previousLog;
        writeContents(GLOBAL_LOG_FILE, newLog);
    }

    private static String writeMergeLog(String commitID, Commit commit, String splitID){
        String Date = commit.getTimestamp();
        String message = commit.getMessage();
        String context = mergeLogcontext(commitID, Date, message, splitID);
        String previousLog = readContentsAsString(GLOBAL_LOG_FILE);
        String newLog = context + previousLog;
        writeContents(GLOBAL_LOG_FILE, newLog);
        return newLog;
    }

    private static String commitLogcontext(String commitID, String Date, String massage){
        String context = "===\n" +
                "commit " + commitID + "\n" +
                "Date: " + Date + "\n"
                + massage + "\n\n";
        return context;
    }

    private static String mergeLogcontext(String commitID, String Date, String massage, String splitID){
        String context = "===\n" +
                "commit " + commitID + "\n" +
                "Merge: " + splitID + " " + commitID + "\n" +
                "Date: " + Date + "\n"
                + massage + "\n\n";
        return context;
    }

    private static void backupFile(Commit commit, String fileName){
        Map<String, String> headBlobs = commit.getBlobsID();
        if (!headBlobs.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String blobsID = headBlobs.get(fileName);
        File blobFile = join(BLOBS_DIR, blobsID);
        byte[] content = readContents(blobFile);
        File fileToCheckout = join(CWD, fileName);
        writeContents(fileToCheckout, content);
    }

    private static Commit getCommit(String branchName) {
        File branchFile = join(HEADS_DIR, branchName);
        String commitID = readContentsAsString(branchFile);
        File commitFile = join(COMMITS_DIR, commitID);
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
    }

    /** Checkout all files in the target commit to the working directory.
     * @param targetCommit
     * @return true if checkout is successful, false if there are untracked files in the way.
     */
    private static boolean checkoutTree(Commit targetCommit) {
        Commit currentCommit = readObject(getHeadCommitFile(), Commit.class);
        Map<String, String> currentBlobs = currentCommit.getBlobsID();
        Map<String, String> targetBlobs = targetCommit.getBlobsID();

        if (currentBlobs == null) currentBlobs = new HashMap<>();
        if (targetBlobs == null) targetBlobs = new HashMap<>();

        List<String> workingFiles = listWorkingFiles();
        for (String fileName : workingFiles) {
            boolean isTrackedCurrent = currentBlobs.containsKey(fileName);
            boolean isInTarget = targetBlobs.containsKey(fileName);
            if (!isTrackedCurrent && isInTarget) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return false;
            }
        }


        for (String fileName : targetBlobs.keySet()) {
            backupFile(targetCommit, fileName);
        }

        // Delete files tracked in current but not in target
        for (String fileName : currentBlobs.keySet()) {
            if (!targetBlobs.containsKey(fileName)) {
                Utils.restrictedDelete(join(CWD, fileName));
            }
        }

        return true;
    }

    private static Commit findSplitPoint(Commit commit1, Commit commit2) {
        Set<String> visited = new HashSet<>();
        while (true) {
            String commit1ID = Utils.sha1(Utils.serialize(commit1));
            visited.add(commit1ID);
            String parentID = commit1.getParent();
            if (parentID == null) {
                break;
            }
            File parentFile = join(COMMITS_DIR, parentID);
            commit1 = Utils.readObject(parentFile, Commit.class);
        }
        while (true) {
            String commit2ID = Utils.sha1(Utils.serialize(commit2));
            if (visited.contains(commit2ID)) {
                return commit2;
            }
            String parentID = commit2.getParent();
            if (parentID == null) {
                break;
            }
            File parentFile = join(COMMITS_DIR, parentID);
            commit2 = Utils.readObject(parentFile, Commit.class);
        }
        return null;
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
        String prev =readContentsAsString(GLOBAL_LOG_FILE);
        writeContents(GLOBAL_LOG_FILE, context + prev);
    }


/*********************************************************************************************/

    /** List all files in the working directory recursively, excluding .gitlet directory.
     * @return List of file paths relative to the working directory.
     * @Author ChatGPT
     */
    private static List<String> listWorkingFiles() {
        List<String> files = new ArrayList<>();
        collectWorkingFiles(CWD, "", files);
        return files;
    }

    private static void collectWorkingFiles(File dir, String prefix, List<String> out) {
        File[] entries = dir.listFiles();
        if (entries == null) {
            return;
        }
        for (File entry : entries) {
            if (entry.getName().equals(".gitlet")) {
                continue;
            }
            String relPath = prefix.isEmpty()
                    ? entry.getName()
                    : prefix + File.separator + entry.getName();
            if (entry.isDirectory()) {
                collectWorkingFiles(entry, relPath, out);
            } else {
                out.add(relPath);
            }
        }
    }

    private static String getFullCommitID(String commitID, List<String> commitIDs) {
        String findCommitID = null;
        for (String id : commitIDs) {
            if (id.startsWith(commitID)) {
                findCommitID = id;
                break;
            }
        }
        if (findCommitID == null) {
            System.out.println("No commit with that id exists.");
            return null;
        }
        return findCommitID;
    }
}
