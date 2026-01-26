package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import java.util.HashMap;
import java.util.Map;


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
    /** @return File object of the head commit.
     */
    public static File getHeadCommitFile() {
        String currentBranch = readContentsAsString(HEAD_FILE);
        File branchFile = join(HEADS_DIR, currentBranch);
        String headCommitID = readContentsAsString(branchFile);
        return join(COMMITS_DIR, headCommitID);
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
        String parentID = Utils.sha1(Utils.serialize(parentCommit));
        Commit newCommit = new Commit(msg, parentID);

        /* Inherit parentcommit's blobsID */
        HashMap<String, String> parentBlobsID = parentCommit.getBlobsID();
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

    /** Update the current branch to point to the new commit ID.
     * @param newCommitID
     */
    public static void updateHead(String newCommitID) {
        String currentBranch = readContentsAsString(HEAD_FILE);
        File branchFile = join(HEADS_DIR, currentBranch);
        writeContents(branchFile, newCommitID);
    }

}
