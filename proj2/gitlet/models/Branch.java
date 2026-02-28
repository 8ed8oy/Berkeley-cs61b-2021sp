package gitlet.models;

import java.io.Serializable;

/** Represents a branch reference (name -> head commit id). */
public class Branch implements Serializable {
    private final String name;
    private final String headCommitId;

    public Branch(String name, String headCommitId) {
        this.name = name;
        this.headCommitId = headCommitId;
    }

    public String getName() {
        return name;
    }

    public String getHeadCommitId() {
        return headCommitId;
    }
}
