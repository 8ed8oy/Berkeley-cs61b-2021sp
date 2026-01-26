package gitlet;

// TODO: any imports you need here

import net.sf.saxon.functions.Serialize;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    private HashMap<String, String> blobsID = new HashMap<>();

    public Commit (String massage, String parent) {
        this.message = massage;
        if (parent == null) {
            this.timestamp = "00:00:00 UTC, Thursday, 1 January 1970";
        } else {
            this.timestamp = new Date().toString();
        }
        this.parent = parent;
    }

    public void save(){
        String commitID = Utils.sha1(serialize(this));
        File commitFile = Utils.join(Repository.COMMITS_DIR, commitID);
        Utils.writeObject(commitFile, this);
    }

    public HashMap<String, String> getBlobsID() {
        return this.blobsID;
    }

    public  String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getParent() {
        return this.parent;
    }
}
