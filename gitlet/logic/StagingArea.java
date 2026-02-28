package gitlet.logic;

import gitlet.Utils;
import gitlet.models.Commit;
import gitlet.storage.Persistence;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Utils.*;

/**
 * The staging area keeps track of files that are to be added or removed
 * in the next commit.
 * Only files that are new or modified compared to the current commit
 * will be stored in the staging area.
 * If a file already matches the current commit, it will not be staged.
 * If a file is the same as the current commit, it will be removed from the staging area.
 */
public class StagingArea implements Serializable {
    private Map<String, String> addedFiles = new HashMap<>();
    private Set<String> removedFiles = new HashSet<>();

    /**
     * Adds a file to the staging area with the potential for addition or modification.
     * Logic flow:
     * 1. Reads the file from disk (Computing the Blob).
     * 2. Compares with the current commit.
     * 3. Updates the Staging Area status (clearing 'removed' status if present).
     * 4. Persists the Blob content if it's new.
     */
    public void add(String fileName) {
        File file = new File(Persistence.CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        byte[] content = readContents(file);
        String blobsID = sha1((Object) content);

        // If it was previously marked waiting for removal, unmark it.
        if (removedFiles.contains(fileName)) {
            removedFiles.remove(fileName);
        }

        // If the file is the same as the current commit, unstage it.
        Commit headCommit = Persistence.readHeadCommit();
        Map<String, String> headBlobs = (headCommit != null) ? headCommit.getBlobsID() : null;

        if (headBlobs != null && headBlobs.containsKey(fileName) && headBlobs.get(fileName).equals(blobsID)) {
            addedFiles.remove(fileName);
            save();
            return;
        }

        Persistence.saveBlob(content);

        addedFiles.put(fileName, blobsID);
        save();
    }

    public boolean isEmpty() {
        return addedFiles.isEmpty() && removedFiles.isEmpty();
    }

    // Save the current staging area to the index file
    public void save() {
        Persistence.saveStagingArea(this);
    }

    // Load the staging area from the index file
    public static StagingArea fromFile() {
        return Persistence.readStagingArea();
    }

    public void rm(String fileName) {
        Commit headCommit = Persistence.readHeadCommit();
        boolean isStaged = addedFiles.containsKey(fileName);
        boolean isTracked = headCommit != null && headCommit.getBlobsID().containsKey(fileName);

        if (!isStaged && !isTracked) {
            System.out.println("No reason to remove the file.");
            return;
        }

        if (isStaged) {
            addedFiles.remove(fileName);
        }

        if (isTracked) {
            removedFiles.add(fileName);
            File fileToDelete = join(Persistence.CWD, fileName);
            if (fileToDelete.exists()) {
                Utils.restrictedDelete(fileToDelete);
            }
        }
        save();
    }

    public void clear() {
        addedFiles.clear();
        removedFiles.clear();
        save();
    }

    public Map<String, String> getAddedFiles() {
        return this.addedFiles;
    }

    public Set<String> getRemovedFiles() {
        return this.removedFiles;
    }
}
