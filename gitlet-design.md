# Gitlet Design Document

**Name**:Fangheng Wang

## Structure
```
gitlet/
├── Main.java           // 解析命令行参数，调用 Repository 的方法
├── Repository.java     // 核心入口，持有下面所有组件的实例
├── models/             // 领域模型
│   ├── Commit.java
│   ├── Blob.java
│   └── Branch.java     
├── logic/              // 复杂业务逻辑
│   ├── StagingArea.java // 处理 Add/Rm 逻辑
│   ├── Merger.java      // 专门处理 Merge 的 8 种情况
│   └── HistoryWalker.java // 处理 Log, Find, SplitPoint (BFS/DFS 逻辑)
└── storage/            // 基础设施
    ├── Persistence.java // 封装 Utils.readObject/writeObject
    ├── ReferenceStore.java // 管理 .gitlet/refs/
    └── ObjectStore.java    // 管理 .gitlet/objects/ (blobs & commits)
```

## 设计原则
1. **单一职责**：每个类/模块只负责一个功能，降低耦合度。
2. **模块化**：将不同功能划分到不同包中，便于维护和扩展。
3. **封装**：隐藏内部实现细节，提供清晰的接口供外部调用。
4. **面向对象**：使用类和对象来建模 Gitlet 的核心概念（如 Commit、Blob、Branch），使代码更易理解和维护。

## 
