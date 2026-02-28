package gitlet.logic;

import gitlet.Utils;
import gitlet.models.Commit;
import gitlet.storage.Persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

/**
 * Working directory operations: checkout files, checkout trees, and deletions.
 */
public class WorkingTree {

    public static void checkoutFile(Commit commit, String fileName) {
        Map<String, String> headBlobs = commit.getBlobsID();
        if (!headBlobs.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        String blobsID = headBlobs.get(fileName);
        File blobFile = join(Persistence.BLOBS_DIR, blobsID);
        byte[] content = readContents(blobFile);
        File fileToCheckout = join(Persistence.CWD, fileName);
        writeContents(fileToCheckout, content);
    }

    /**
     * Checkout all files from target commit to working directory.
     * Returns false if untracked files would be overwritten.
     */
    public static boolean checkoutTree(Commit targetCommit) {
        Commit currentCommit = Persistence.readHeadCommit();
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
            checkoutFile(targetCommit, fileName);
        }

        for (String fileName : currentBlobs.keySet()) {
            if (!targetBlobs.containsKey(fileName)) {
                Utils.restrictedDelete(join(Persistence.CWD, fileName));
            }
        }

        return true;
    }

    /** List all files in the working directory recursively, excluding .gitlet directory. */
    private static List<String> listWorkingFiles() {
        List<String> files = new ArrayList<>();
        collectWorkingFiles(Persistence.CWD, "", files);
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
}
