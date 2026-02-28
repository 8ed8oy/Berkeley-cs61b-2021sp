package gitlet.models;

import java.io.Serializable;

/** Represents a snapshot of file content (blob). */
public class Blob implements Serializable {
    private final byte[] content;

    public Blob(byte[] content) {
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }
}
