# Gitlet Design Document

**Name**: Fangheng Wang

## 1. Classes and Data Structures

### `Main`
The entry point of the application. It parses command-line arguments and delegates commands to the `Repository`.

### `Repository`
The central controller that coordinates all Gitlet operations. It maintains the state of the current working directory and interacts with the storage and logic layers.
- **Fields**:
  - `String CWD`: Current Working Directory.
  - `File GITLET_DIR`: The `.gitlet` directory.
- **Key Methods**:
  - `init()`: Initializes a new Gitlet repository.
  - `add(String filename)`: Stages a file for addition.
  - `commit(String message)`: Saves a snapshot of tracked files.
  - `checkout(...)`: Restores files from commit history.

### `models.Commit`
Represents a snapshot of the project's files.
- **Fields**:
  - `String message`: Log message.
  - `String timestamp`: Commit time.
  - `String parent`: SHA-1 hash of the parent commit.
  - `Map<String, String> blobs`: Mapping of file names to blob SHA-1 hashes.

### `logic.StagingArea`
Manages the staging area (index), tracking files to be added or removed in the next commit.
- **Mechanism**: Serializes a `StagingArea` object to `.gitlet/index`.

### `logic.HistoryWalker`
Helper class for traversing the commit graph.
- **Responsibilities**:
  - Printing logs (`log`, `global-log`).
  - Finding split points for merge operations.
  - Locating commits by message.

### `storage.Persistence` & `storage.ObjectStore`
Handles the serialization and deserialization of objects (Commits, Blobs) to the disk.
- **Structure**:
  - `.gitlet/objects/`: Stores content addressable data (blobs and commits).
  - `.gitlet/refs/`: Stores pointers to branch heads (e.g., `regs/heads/master`).

## 2. Algorithms

### `add`
1. Compute the SHA-1 hash of the file's current content.
2. If the file is identical to the version in the current commit, remove it from the staging area.
3. Otherwise, save the blob to the object store and update the staging area mapping.

### `merge`
Uses the `HistoryWalker` to find the split point (latest common ancestor) between the current branch and the given branch.
- **Rules**: Implements the 8 distinct merge conflict/resolution rules specified in the assignment.

## 3. Persistence
The state of Gitlet is persisted in the `.gitlet` directory.
- **Commits & Blobs**: Stored as files named by their SHA-1 hash in `objects/`.
- **Index**: The staging area is serialized to a file.
- **HEAD**: A text file pointing to the current branch reference.
