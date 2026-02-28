package gitlet.storage;

import gitlet.models.Commit;

/**
 * Manages .gitlet/objects/ (commits & blobs).
 * This class can be expanded to replace direct Persistence calls.
 */
public class ObjectStore {

    public static void saveCommit(Commit commit) {
        Persistence.saveCommit(commit);
    }

    public static Commit readCommit(String commitId) {
        return Persistence.readCommit(commitId);
    }

    public static void saveBlob(byte[] content) {
        Persistence.saveBlob(content);
    }

    public static byte[] readBlob(String blobId) {
        return Persistence.readBlob(blobId);
    }
}
