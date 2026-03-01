# Gitlet

A version-control system that mimics some basic features of Git, implemented in Java.

## Features
- **Init**: Initialize a new repository.
- **Add/Rm**: Stage files for commit or remove them.
- **Commit**: Create snapshots with messages and timestamps.
- **Log/Global-Log**: View commit history.
- **Branch/Checkout**: Create and switch between branches.
- **Merge**: Merge changes from different branches with conflict handling.
- **Reset**: Reset repository to a specific commit.

## Build
You can build the project using Make (recommended) or Maven.

### With Make
```bash
make
```

### With Maven
```bash
mvn clean package
```
This will generate `gitlet-1.0-SNAPSHOT.jar` (or similar) in the `target/` directory.

## Usage

### Windows
Run `gitlet.bat` followed by commands:
```cmd
.\gitlet.bat init
.\gitlet.bat add file.txt
.\gitlet.bat commit "Initial commit"
```

### Linux / Mac
Run the `gitlet.sh` script:
```bash
chmod +x gitlet.sh
./gitlet.sh init
./gitlet.sh add file.txt
./gitlet.sh commit "Initial commit"
```

## Java Requirement
Requires Java 15 or higher.

## Author
Fangheng Wang
