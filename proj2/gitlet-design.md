# Gitlet Design Document

**Name**:Fangheng Wang

## Classes and Data Structures

### Main

#### Fields



### commit
- Massage : contains the commit message
- Time : contains the time a commit was made
- Parent : the parent commit of a commit object
- Blob : a mapping of file names to blob ids

#### Fields
- `Save` () : Serializes and saves the commit object to disk.


### Repository

#### Fields



### StagingArea

**Responsibilities**: Manages files staged for addition or removal. Persisted at `.gitlet/index`.

**Fields**:
- `addedFiles (Map<String, String>)`: Filename -> Blob Hash.
- `removedFiles (Set<String>)`: Filenames marked for removal.

**Core Methods**:
- `add(String filename)`:
    1. Hash file content.
    2. Remove from `removedFiles` if present.
    3. If hash differs from current commit, update `addedFiles`; otherwise remove from stage.
    4. Persist Blob and Index.


### Utils
- UID_LENGTH : the length of the unique identifier, which is 40.

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

## Testing Plan

### Use-Case Scenarios

Here we outline the integration tests for key commands.

#### 1. `init`
| Scenario | Steps | Expected Outcome |
| :--- | :--- | :--- |
| **Basic Init** | 1. `init` | 1. `.gitlet` directory created.<br>2. Initial commit created (verify via log or file check). |
| **Re-Init** | 1. `init`<br>2. `init` | Output: "A Gitlet version-control system already exists in the current directory." |

#### 2. `add`
| Scenario | Steps | Expected Outcome |
| :--- | :--- | :--- |
| **Add New File** | 1. Create `wug.txt`<br>2. `add wug.txt` | File added to `.gitlet/index` (staging). Blob created in `.gitlet/objects`. |
| **Unchange** | 1. `add wug.txt` (same content as HEAD) | File removed from staging area if previously staged. Not marked for addition. |
| **Override Staging** | 1. `add wug.txt` (v1)<br>2. Modify `wug.txt` (v2)<br>3. `add wug.txt` | Blob for v2 replaces v1 in staging. |
| **Un-Remove** | 1. `rm wug.txt`<br>2. `add wug.txt` | File removed from `removedFiles` set. |
| **Missing File** | 1. `add doesnotexist.txt` | Output: "File does not exist." |

