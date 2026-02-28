package gitlet;

import gitlet.logic.StagingArea;
import gitlet.storage.Persistence;
import gitlet.models.Commit;

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
import gitlet.logic.HistoryWalker;
import gitlet.logic.Merger;
import gitlet.storage.ReferenceStore;
import gitlet.logic.WorkingTree;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Fangheng Wang
 */
public class Repository {

    public static void checkInitialized() {
        if (!Persistence.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void init() {
        if (Persistence.GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        Persistence.setupPersistence();

        /* create and save initial commit */
        Commit initialCommit = new Commit("initial commit", null);
        Persistence.saveCommit(initialCommit);

        // initialCommit ID calculation is needed for master branch setup
        String commitID = Utils.sha1(serialize(initialCommit));

        /* create master branch pointing to initial commit */
        ReferenceStore.createBranch("master", commitID);

        /* write initial log */
        writeCommitLog(commitID, initialCommit);

        /* set HEAD to master */
        ReferenceStore.setCurrentBranchName("master");
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
        File fileToAdd = join(Persistence.CWD, fileName);

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

        Commit parentCommit = Persistence.readHeadCommit();
        // The parent of the new commit is the current HEAD commit ID.
        String parentID = ReferenceStore.getHeadCommitId();

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
        Persistence.saveCommit(newCommit); // Use Persistence to save

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
        stagingArea.rm(fileName);
    }

    public static void log(){
        checkInitialized();
        HistoryWalker.printLog();
    }

    /** Global log is stored in GLOBAL_LOG_FILE, which is different from the doc. */
    public static void globalLog(){
        checkInitialized();
        if (Persistence.GLOBAL_LOG_FILE.exists()) {
            String globalLog = readContentsAsString(Persistence.GLOBAL_LOG_FILE);
            System.out.print(globalLog);
        }
    }

    public static void find(String message){
        checkInitialized();
        List<String> matches = HistoryWalker.findCommitIdsByMessage(message);
        if (matches.isEmpty()) {
            System.out.println("Found no commit with that message.");
            return;
        }
        for (String commitID : matches) {
            System.out.println(commitID);
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
        List<String> allFiles = Utils.plainFilenamesIn(Persistence.CWD);
        if (allFiles == null) allFiles = new ArrayList<>();

        Set<String> allFilesSet = new HashSet<>(allFiles);
        Map<String, String> addedFiles = stagingArea.getAddedFiles();
        Set<String> removedFiles = stagingArea.getRemovedFiles();

        /** get blobsmap from headcommit. */
        Commit headCommit = Persistence.readHeadCommit();
        Map<String, String> headBlobs = (headCommit != null) ? headCommit.getBlobsID() : new HashMap<>();

        Map<String, String> workingBlobs = new HashMap<>();
        for (String fileName : allFiles) {
            File file = join(Persistence.CWD, fileName);
            byte[] content = readContents(file);
            workingBlobs.put(fileName, Utils.sha1((Object) content));
        }

        String currentBranch = ReferenceStore.getCurrentBranchName();
        List<String> branchNames = ReferenceStore.listBranches();
        if (branchNames == null) branchNames = new ArrayList<>();
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
        WorkingTree.checkoutFile(headCommit, fileName);
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
        List<String> commitIDs = Utils.plainFilenamesIn(Persistence.COMMITS_DIR);

        String findCommitID = getFullCommitID(shortCommitID, commitIDs);
        if (findCommitID == null) return;
        File commitFile = join(Persistence.COMMITS_DIR, findCommitID);
        Commit findCommit = Utils.readObject(commitFile, Commit.class);
        WorkingTree.checkoutFile(findCommit, fileName);
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
        File branchFile = ReferenceStore.getBranchFile(branchName);
        if (!branchFile.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        String currentBranch = ReferenceStore.getCurrentBranchName();
        if (currentBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit commit = getCommit(branchName);

        if (!WorkingTree.checkoutTree(commit)) {
            return;
        }

        ReferenceStore.setCurrentBranchName(branchName);
        StagingArea stagingArea = StagingArea.fromFile();
        stagingArea.clear();
    }

    public static void branch(String branchName){
        checkInitialized();
        if (branchName == null || branchName.isEmpty()) {
            System.out.println("Branch name cannot be empty.");
            return;
        }
        if (ReferenceStore.branchExists(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        String headCommitID = ReferenceStore.getHeadCommitId();
        ReferenceStore.createBranch(branchName, headCommitID);
    }

    public static void rmBranch(String branchName){
        checkInitialized();
        if (branchName == null || branchName.isEmpty()) {
            System.out.println("Branch name cannot be empty.");
            return;
        }
        if (!ReferenceStore.branchExists(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        String currentBranch = ReferenceStore.getCurrentBranchName();
        if (currentBranch.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        ReferenceStore.deleteBranch(branchName);
    }

    public static void reSet(String commitIDToReset) {
        checkInitialized();
        if (commitIDToReset == null || commitIDToReset.isEmpty()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        List<String> commitIDs = Utils.plainFilenamesIn(Persistence.COMMITS_DIR);
        String findCommitID = getFullCommitID(commitIDToReset, commitIDs);
        if (findCommitID == null) return;

        File commitFile = join(Persistence.COMMITS_DIR, findCommitID);
        Commit targetCommit = Utils.readObject(commitFile, Commit.class);

        if (!WorkingTree.checkoutTree(targetCommit)) {
            return;
        }

        ReferenceStore.updateHead(findCommitID);

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
        Merger.merge(targetBranch);
    }

    /*****************************************************************************************/
    /**                               Helper methods.                                        */
    /*****************************************************************************************/

    /** @return File object of the head commit.
     */
    public static File getHeadCommitFile() {
        String currentBranch = ReferenceStore.getCurrentBranchName();
        String headCommitID = ReferenceStore.readBranchHead(currentBranch);
        return join(Persistence.COMMITS_DIR, headCommitID);
    }

    /** Update the current branch to point to the new commit ID.
     * @param newCommitID
     */
    private static void updateHead(String newCommitID) {
        ReferenceStore.updateHead(newCommitID);
    }

    private static void writeCommitLog(String commitID, Commit commit ){
        String Date = commit.getTimestamp();
        String message = commit.getMessage();
        String context = commitLogcontext(commitID, Date, message);
        String previousLog = "";
        if (Persistence.GLOBAL_LOG_FILE.exists()) {
            previousLog = readContentsAsString(Persistence.GLOBAL_LOG_FILE);
        }
        String newLog = context + previousLog;
        writeContents(Persistence.GLOBAL_LOG_FILE, newLog);
    }

    private static String commitLogcontext(String commitID, String Date, String massage){
        String context = "===\n" +
                "commit " + commitID + "\n" +
                "Date: " + Date + "\n"
                + massage + "\n\n";
        return context;
    }

    private static Commit getCommit(String branchName) {
        String commitID = ReferenceStore.readBranchHead(branchName);
        File commitFile = join(Persistence.COMMITS_DIR, commitID);
        Commit commit = readObject(commitFile, Commit.class);
        return commit;
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
