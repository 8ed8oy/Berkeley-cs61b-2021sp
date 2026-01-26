# Gitlet Design Document

**Name**:Fangheng Wang

## Classes and Data Structures

### Main

#### Fields



### commit
* Massage - contains the commit message
* Time - contains the time a commit was made
* Parent - the parent commit of a commit object

#### Fields



### Repository

#### Fields



### StagingArea

#### Fields

1.  `addedFiles` (`Map<String, String>`):
    *   **Key**: The relative file path.
    *   **Value**: The SHA-1 hash of the file content (Blob ID).
    *   **Purpose**: Tracks files that have been staged for addition or modification.
2.  `removedFiles` (`Set<String>`):
    *   **Key**: The relative file path.
    *   **Purpose**: Tracks files that have been staged for removal.

### Utils

#### Fields



### GitletEception

### Dumpable

### DumpObject


## Algorithms

### StagingArea.add(String fileName)
1.  Read the file content from CWD.
2.  Compute the SHA-1 hash of the content.
3.  Check if the file is already recorded in the `removedFiles` set. If so, remove it from there (unstage the removal).
4.  Case check: Compare the new hash with the hash of this file in the **current commit** (HEAD).
    *   If they are the same, this file is not "new" or "modified". Remove it from `addedFiles` if it was there. do not add it.
    *   If they are different, update `addedFiles` with the new hash.
5.  Save the blob to the objects directory if it doesn't exist.
6.  Serialize and save the `StagingArea` object to the index file.

### Repository.add(String fileName)
1. Load the current `StagingArea` from disk.
2. Invoke `StagingArea.add(fileName)`.
3. Save the `StagingArea` to disk.

## Persistence

### Staging Area
*   **Strategy**: Implements `Serializable`.
*   **Location**: Saved in `.gitlet/index`.
*   **Lifecycle**:
    *   Loaded from disk at the start of any command that manipulates the stage.
    *   Saved back to disk immediately after modification.
    *   Better than saved in separate directory because it simplifies management and reduces file clutter.
