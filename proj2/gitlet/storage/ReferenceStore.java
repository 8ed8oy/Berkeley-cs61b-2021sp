package gitlet.storage;

import gitlet.Utils;

import java.io.File;
import java.util.List;

import static gitlet.Utils.*;

/**
 * Manages .gitlet/refs/ (branches & HEAD).
 */
public class ReferenceStore {

    public static String getCurrentBranchName() {
        if (!Persistence.HEAD_FILE.exists()) return null;
        return readContentsAsString(Persistence.HEAD_FILE).trim();
    }

    public static void setCurrentBranchName(String branchName) {
        writeContents(Persistence.HEAD_FILE, branchName);
    }

    public static String getHeadCommitId() {
        String branchName = getCurrentBranchName();
        if (branchName == null) return null;
        return readBranchHead(branchName);
    }

    public static void updateHead(String newCommitId) {
        String branchName = getCurrentBranchName();
        if (branchName == null) return;
        writeBranchHead(branchName, newCommitId);
    }

    public static File getBranchFile(String branchName) {
        return join(Persistence.HEADS_DIR, branchName);
    }

    public static boolean branchExists(String branchName) {
        return getBranchFile(branchName).exists();
    }

    public static List<String> listBranches() {
        return Utils.plainFilenamesIn(Persistence.HEADS_DIR);
    }

    public static String readBranchHead(String branchName) {
        File branchFile = getBranchFile(branchName);
        if (!branchFile.exists()) return null;
        return readContentsAsString(branchFile).trim();
    }

    public static void writeBranchHead(String branchName, String commitId) {
        File branchFile = getBranchFile(branchName);
        writeContents(branchFile, commitId);
    }

    public static void createBranch(String branchName, String commitId) {
        writeBranchHead(branchName, commitId);
    }

    public static void deleteBranch(String branchName) {
        Utils.restrictedDelete(getBranchFile(branchName));
    }
}
