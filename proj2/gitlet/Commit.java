package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Utils.serialize;

/** Represents a gitlet commit object.
 * Commits have a message, a timestamp, a parent commit, and UID.
 * A commit includes UIDs of all files tracked in the commit using HashMap.
 * key: file name, value: file's blobs UID.
 *  @author Fangheng Wang
 */
public class Commit implements Serializable {
    private String message;
    private String timestamp;
    private String parent;
    private String secondParent; // used in merge commits
    private TreeMap<String, String> blobsID = new TreeMap<>();

    public Commit (String massage, String parent) {
        this.message = massage;
        if (parent == null) {
            this.timestamp = dateToTimeStamp(new Date(0));
        } else {
            this.timestamp = dateToTimeStamp(new Date());
        }
        this.parent = parent;
    }

    private String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.CHINA);
        return dateFormat.format(date);
    }

    public void save(){
        String commitID = Utils.sha1(serialize(this));
        File commitFile = Utils.join(Repository.COMMITS_DIR, commitID);
        Utils.writeObject(commitFile, this);
    }

    public TreeMap<String, String> getBlobsID() {
        return this.blobsID;
    }

    public  String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setBlobsID(Map<String, String> blobsID) {
        if (blobsID == null) {
            throw new IllegalArgumentException("blobsID cannot be null");
        }
        this.blobsID = new TreeMap<>(blobsID);
    }

    public String getParent() {return this.parent;}

    public  String getSecondParent() {return this.secondParent;}
}
