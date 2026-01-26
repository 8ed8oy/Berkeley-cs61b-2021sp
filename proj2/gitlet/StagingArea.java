package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Utils.*;

/**
 * The staging area keeps track of files that are to be added or removed
 * in the next commit.
 * Only files that are new or modified compared to the current commit
 * weill be stored in the staging area.
 * If a file already matches the current commit, it will not be staged.
 * if a file is the same as the current commit, it will be removed from the staging area.
 * @author Fangheng Wang
 */
public class StagingArea implements Serializable {
    private Map<String, String> addedFiles = new HashMap<>();
    private Set<String> removedFiles = new HashSet<>();

    /**
     * Adds a file to the staging area with the potential for addition or modification.
     * <p>
     * Logic flow:
     * 1. Reads the file from disk (Computing the Blob).
     * 2. Compares with the current commit .
     * 3. Updates the Staging Area status (clearing 'removed' status if present).
     * 4. Persists the Blob content if it's new.
     */
    public void add(String fileName) {
        File file = new File(Repository.CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        byte[] content = readContents(file);
        String blobsID = sha1((Object) content);
        File blobFile = join(Repository.BLOBS_DIR, blobsID);

        // Logic: If it was previously marked waiting for removal, unmark it.
        if (removedFiles.contains(fileName)) {
            removedFiles.remove(fileName);
        }

        // Logic: If the file is the same as the current commit, unstage it.
        File commitFile = Repository.getHeadCommitFile();
        Commit headCommit = readObject(commitFile, Commit.class);
        HashMap<String, String> headBlobs = headCommit.getBlobsID();
        if (headBlobs.containsKey(fileName) && headBlobs.get(fileName).equals(blobsID)) {
            addedFiles.remove(fileName);
            return;
        }

        if (!blobFile.exists()) {
            writeContents(blobFile, content);
        }

        addedFiles.put(fileName, blobsID);
    }

    // Save the current staging area to the index file
    public void save() {
        writeObject(Repository.INDEX_FILE, this);
    }

    // Load the staging area from the index file
    public static StagingArea fromFile() {
        if (!Repository.INDEX_FILE.exists()) {
            return new StagingArea();
        }
        return readObject(Repository.INDEX_FILE, StagingArea.class);
    }

    public void rm(String fileName) {

    }

    public void clear() {
        addedFiles.clear();
        removedFiles.clear();
    }

}
