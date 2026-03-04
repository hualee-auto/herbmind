# HerbMind 数据版本同步机制

## 概述

已实现基于版本号的中药数据同步机制，应用启动时会自动检查云端版本并同步数据。

## 核心变更

### 1. 版本文件

**文件位置**: `resources/final_data/version.json`

```json
{
  "version": 1,
  "lastUpdated": 1741008000000,
  "herbCount": 606,
  "description": "初始版本，包含606味中药数据",
  "minAppVersion": "1.0.0"
}
```

### 2. 数据同步流程

```
应用启动
    ↓
检查远程版本 (GitHub Raw)
    ↓
远程成功？
    ↓ 是          ↓ 否
获取远程数据   使用本地 Assets
    ↓                ↓
比较版本号      比较版本号
    ↓                ↓
远程 > 本地？   本地 > 数据库？
    ↓ 是          ↓ 是
同步到数据库   同步到数据库
    ↓                ↓
更新版本号      更新版本号
    ↓                ↓
  完成
```

### 3. 关键类说明

| 类名 | 路径 | 说明 |
|------|------|------|
| `HerbDataSyncUseCase` | `domain/sync/` | 数据同步核心逻辑 |
| `AppDataInitializer` | `domain/sync/` | 应用启动时初始化 |
| `HerbRemoteDataSource` | `data/remote/` | 远程数据源接口 |
| `GitHubRawDataSource` | `data/remote/` | GitHub Raw 实现（Ktor HTTP） |
| `LocalJsonDataSource` | `data/remote/` | 本地 Assets 实现（fallback） |
| `DataVersionInfo` | `domain/sync/` | 版本信息数据类 |

### 4. 数据库表

**data_version 表** - 存储版本信息

```sql
CREATE TABLE data_version (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    version INTEGER NOT NULL DEFAULT 0,
    lastSyncAt INTEGER,
    remoteVersion INTEGER DEFAULT 0
);
```

### 5. 依赖注入配置

```kotlin
// 远程数据源
single<HerbRemoteDataSource>(named("remote")) { GitHubRawDataSource() }

// 本地数据源
single<HerbRemoteDataSource>(named("local")) { LocalJsonDataSource(get<Context>()) }

// 同步 UseCase（同时注入远程和本地）
factory { 
    HerbDataSyncUseCase(
        database = get(),
        remoteDataSource = get(named("remote")),
        localDataSource = get(named("local"))
    )
}
```

### 6. 应用启动同步

**HerbMindApplication.kt** 中自动执行：

```kotlin
override fun onCreate() {
    super.onCreate()
    
    // 初始化 Koin
    startKoin { ... }
    
    // 执行数据同步（后台线程）
    initializeDataSync()
}
```

## 使用方式

### 更新数据步骤

1. **修改药材数据**: 更新 `resources/final_data/herbs_split.json`
2. **增加版本号**: 修改 `resources/final_data/version.json` 中的 `version` 字段
3. **提交到 GitHub**: 推送代码到 main 分支
4. **更新 APK 数据**: 将新的 JSON 文件放入 `androidApp/src/main/assets/final_data/`

### 版本递增规则

- **Bug 修复/数据纠错**: 版本号 +1
- **新增药材**: 版本号 +10
- **重大结构变更**: 版本号 +100

## 技术细节

### Ktor HTTP 客户端配置

```kotlin
val client = HttpClient {
    install(ContentNegotiation) {
        json(json)
    }
}
```

### 同步结果处理

```kotlin
when (result) {
    is SyncResult.InProgress -> { /* 显示进度 */ }
    is SyncResult.Success -> { /* 同步完成 */ }
    is SyncResult.NoUpdate -> { /* 已是最新 */ }
    is SyncResult.Error -> { /* 同步失败 */ }
}
```

## 文件变更清单

### 新增文件
- `shared/src/androidMain/kotlin/com/herbmind/data/remote/LocalJsonDataSource.kt`

### 修改文件
- `gradle/libs.versions.toml` - 添加 Ktor 依赖
- `shared/build.gradle.kts` - 添加 Ktor 依赖
- `shared/src/commonMain/kotlin/com/herbmind/data/remote/HerbRemoteDataSource.kt` - 实现 Ktor HTTP
- `shared/src/commonMain/kotlin/com/herbmind/domain/sync/HerbDataSyncUseCase.kt` - 添加本地 fallback
- `shared/src/commonMain/kotlin/com/herbmind/domain/sync/AppDataInitializer.kt` - 优化初始化逻辑
- `shared/src/commonMain/kotlin/com/herbmind/di/KoinModules.kt` - 改为 expect 声明
- `shared/src/androidMain/kotlin/com/herbmind/di/KoinModules.kt` - 实现 actual commonModule
- `androidApp/src/main/kotlin/com/herbmind/android/HerbMindApplication.kt` - 添加启动同步
- `resources/final_data/version.json` - 版本信息文件

### 资源文件变更
- `androidApp/src/main/assets/herbs_split.json` → `androidApp/src/main/assets/final_data/herbs_split.json`
- 新增 `androidApp/src/main/assets/final_data/version.json`

## 注意事项

1. **网络权限**: 确保 AndroidManifest.xml 包含 `INTERNET` 权限
2. **后台同步**: 数据同步在后台线程执行，不阻塞主线程
3. **fallback 机制**: 远程失败时自动使用本地数据
4. **日志输出**: 可在 Logcat 中搜索 "HerbMindApp" 查看同步日志

## 后续优化建议

1. 添加数据同步 UI 提示（首次启动时显示进度）
2. 支持手动检查更新按钮
3. 数据增量更新（只下载变更的部分）
4. 压缩传输（使用 gzip 压缩 JSON）
