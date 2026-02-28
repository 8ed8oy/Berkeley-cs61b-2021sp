package gitlet.storage;

import gitlet.Utils;
import gitlet.logic.StagingArea;
import gitlet.models.Commit;

import java.io.File;

import static gitlet.Utils.*;

/**
 * 负责管理 .gitlet 目录结构以及对象的持久化存储.
 * 所有的 File 路径常量和 readObject/writeObject 调用都应该收敛到这里.
 */
public class Persistence {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    // Objects directory structure
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File COMMITS_DIR = join(OBJECTS_DIR, "commits");
    public static final File BLOBS_DIR = join(OBJECTS_DIR, "blobs");

    // Refs directory structure
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");

    // Special files
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File INDEX_FILE = join(GITLET_DIR, "index");
    public static final File GLOBAL_LOG_FILE = join(GITLET_DIR, "global_log");

    /** 初始化 .gitlet 目录结构 */
    public static void setupPersistence() {
        if (GITLET_DIR.exists()) {
            return;
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
    }

    /* ===================== Commit 存取 ===================== */

    /** 保存 Commit 对象到文件系统 */
    public static void saveCommit(Commit commit) {
        byte[] serialized = serialize(commit);
        String id = sha1((Object) serialized);

        File file = join(COMMITS_DIR, id);
        writeObject(file, commit);
    }

    /** 根据 ID 读取 Commit 对象 */
    public static Commit readCommit(String commitId) {
        if (commitId == null) return null;
        File file = join(COMMITS_DIR, commitId);
        if (!file.exists()) {
            return null;
        }
        return readObject(file, Commit.class);
    }

    /* ===================== Blob 存取 ===================== */

    public static void saveBlob(byte[] content) {
        if (content == null) return;
        String id = sha1((Object) content);
        File file = join(BLOBS_DIR, id);
        if (!file.exists()) {
            writeContents(file, content);
        }
    }

    /** 根据 ID 读取 Blob 内容 */
    public static byte[] readBlob(String blobId) {
        if (blobId == null) return null;
        File file = join(BLOBS_DIR, blobId);
        if (!file.exists()) return null;
        return readContents(file);
    }

    /* ===================== StagingArea 存取 ===================== */

    public static void saveStagingArea(StagingArea stagingArea) {
        writeObject(INDEX_FILE, stagingArea);
    }

    public static StagingArea readStagingArea() {
        if (!INDEX_FILE.exists()) {
            return new StagingArea();
        }
        return readObject(INDEX_FILE, StagingArea.class);
    }

    /* ===================== HEAD / Branch 存取 ===================== */

    /** 获取当前分支名称 */
    public static String getCurrentBranchName() {
        if (!HEAD_FILE.exists()) return null;
        return readContentsAsString(HEAD_FILE).trim();
    }

    /** 获取 HEAD 指向的 Commit ID */
    public static String getHeadCommitId() {
        String branchName = getCurrentBranchName();
        if (branchName == null) return null;

        File branchFile = join(HEADS_DIR, branchName);
        if (!branchFile.exists()) {
            return null;
        }
        return readContentsAsString(branchFile).trim();
    }

    /** 读取 HEAD 指向的 Commit 对象 */
    public static Commit readHeadCommit() {
        String headCommitId = getHeadCommitId();
        if (headCommitId == null) return null;
        return readCommit(headCommitId);
    }

    /** 更新 HEAD 指向的 Commit ID */
    public static void updateHead(String newCommitId) {
        String branchName = getCurrentBranchName();
        if (branchName != null) {
            File branchFile = join(HEADS_DIR, branchName);
            writeContents(branchFile, newCommitId);
        }
    }
}
