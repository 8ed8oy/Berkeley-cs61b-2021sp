package gitlet;

// TODO: any imports you need here

import org.checkerframework.checker.units.qual.C;

import java.io.Serializable;
import java.util.Date;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String timestamp;
    private String parent;
    private int UID;
    String branch;

    /* TODO: fill in the rest of this class. */

    public Commit (String massage, String parent) {
        this.message = massage;
        if (parent == null) {
            this.timestamp = "00:00:00 UTC, Thursday, 1 January 1970";
            this.branch = "master";
        } else {
            this.timestamp = new Date().toString();
        }
        this.parent = parent;
    }

    public  String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public Commit getParent() {
        return this.parent;
    }
}
