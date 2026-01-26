package gitlet;

import java.io.File;
import static gitlet.Utils.*;



/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Fangheng Wang
 */
public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

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

        // create and save initial commit
        Commit initialCommit = new Commit("initial commit", null);
        String commitID = Utils.sha1(serialize(initialCommit));
        File initialCommitFile = join(COMMITS_DIR, commitID);
        writeObject(initialCommitFile, initialCommit);

        // create master branch pointing to initial commit
        File masterBranch = join(HEADS_DIR, "master");
        writeContents(masterBranch, commitID);

        // set HEAD to master
        writeContents(HEAD_FILE, "master");
    }

    public static void add(String fileName) {
        File fileToAdd = join(CWD, fileName);

        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        StagingArea stagingArea = StagingArea.fromFile();
        stagingArea.add(fileName);
        stagingArea.save();
    }

    public static File getHeadCommitFile() {
        String currentBranch = readContentsAsString(HEAD_FILE);
        File branchFile = join(HEADS_DIR, currentBranch);
        String headCommitID = readContentsAsString(branchFile);
        return join(COMMITS_DIR, headCommitID);
    }

}
