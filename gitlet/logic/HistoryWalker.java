package gitlet.logic;

import gitlet.Utils;
import gitlet.models.Commit;
import gitlet.storage.Persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static gitlet.Utils.*;

/**
 * History traversal helpers (log, find, split-point).
 */
public class HistoryWalker {

    public static void printLog() {
        Commit currentCommit = Persistence.readHeadCommit();
        String commitID = Persistence.getHeadCommitId();

        while (currentCommit != null) {
            String date = currentCommit.getTimestamp();
            String message = currentCommit.getMessage();
            System.out.print(formatCommitLog(commitID, date, message));

            String parentID = currentCommit.getParent();
            if (parentID == null) break;

            currentCommit = Persistence.readCommit(parentID);
            commitID = parentID;
        }
    }

    public static List<String> findCommitIdsByMessage(String message) {
        List<String> commitIDs = Utils.plainFilenamesIn(Persistence.COMMITS_DIR);
        List<String> matches = new ArrayList<>();
        if (commitIDs == null) return matches;

        for (String commitID : commitIDs) {
            Commit commit = Persistence.readCommit(commitID);
            if (commit != null && commit.getMessage().equals(message)) {
                matches.add(commitID);
            }
        }
        return matches;
    }

    public static Commit findSplitPoint(Commit commit1, Commit commit2) {
        Set<String> visited = new HashSet<>();
        while (true) {
            String commit1ID = Utils.sha1(Utils.serialize(commit1));
            visited.add(commit1ID);
            String parentID = commit1.getParent();
            if (parentID == null) {
                break;
            }
            File parentFile = join(Persistence.COMMITS_DIR, parentID);
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
            File parentFile = join(Persistence.COMMITS_DIR, parentID);
            commit2 = Utils.readObject(parentFile, Commit.class);
        }
        return null;
    }

    private static String formatCommitLog(String commitID, String date, String message) {
        return "===\n" +
                "commit " + commitID + "\n" +
                "Date: " + date + "\n" +
                message + "\n\n";
    }
}
