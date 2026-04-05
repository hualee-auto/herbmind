# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

HerbMind (本草记) 是一款基于 HKBU（香港浸会大学）中药材数据库的专业中药材查询工具，帮助中医学生、从业者快速精准地查找药材和方剂信息。

- **平台**: Android (minSdk 24, targetSdk 34)
- **UI 框架**: Jetpack Compose
- **编程语言**: Kotlin 1.9.22
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Koin 3.5.3
- **本地存储**: SQLDelight 2.0.1
- **图片加载**: Coil 2.5.0
- **网络请求**: Ktor 2.3.7
- **包名**: `hua.lee.herbmind`（已从 com.herbmind 迁移）

## 核心功能

1. **多维度搜索**: 支持药材名称、拼音、拉丁名、别名、功效、主治、产地、性味搜索
2. **方剂查询**: 支持方剂信息查询及与药材的关联导航
3. **分类浏览**: 按药材类别、产地、性味、归经、功效类别进行筛选
4. **数据同步**: 应用启动时自动从 GitHub Raw 同步最新数据
5. **离线可用**: 基础数据完全离线可用，图片支持本地缓存
6. **国风设计**: 采用竹青、赭石、宣纸白、墨黑的传统中医配色方案

## 数据源

- **药材数据**: HKBU 中药材数据库，420种药材
- **方剂数据**: HKBU 中药方剂数据库
- **数据文件**: `resources/final_data/`（GitHub Raw）
- **本地备份**: `androidApp/src/main/assets/final_data/`
- **图片资源**: 饮片图 (`resources/images/concocted/`)、植物图 (`resources/images/plants/`)

## 项目结构

```
herbmind/
├── androidApp/          # Android 应用模块
│   └── src/main/kotlin/hua/lee/herbmind/android/
│       ├── MainActivity.kt
│       ├── HerbMindApplication.kt
│       └── ui/
│           ├── theme/      # 主题配置 (国风配色)
│           ├── components/ # 可复用组件
│           ├── screens/    # 页面 (Home/Search/HerbDetail/FormulaDetail/Category/Compare/Study)
│           ├── navigation/ # 导航定义
│           └── viewmodel/  # ViewModel
├── shared/              # Kotlin Multiplatform 共享模块
│   └── src/
│       ├── commonMain/kotlin/hua/lee/herbmind/
│       │   ├── data/       # 数据层 (Repository、Model、Remote)
│       │   │   ├── model/  # Herb, Formula 数据类
│       │   │   ├── repository/
│       │   │   ├── local/  # SQLDelight 数据库
│       │   │   └── remote/ # GitHub Raw 数据源
│       │   ├── domain/     # 领域层 (UseCase)
│       │   │   ├── search/
│       │   │   ├── herb/
│       │   │   ├── formula/
│       │   │   └── study/  # 学习功能（V1保留）
│       │   └── di/         # 依赖注入配置
│       ├── androidMain/    # Android 平台特定实现
│       └── commonTest/     # 单元测试
├── resources/            # 资源文件（Git 追踪）
│   └── final_data/       # JSON 数据文件
└── docs/v2-design/      # V2 设计文档
    ├── 01-PRD-HerbMind-V2.md
    ├── 02-UI-Visual-Design.md
    └── 03-Architecture-Design.md
```

## 常用命令

### 构建
```bash
# 构建 Debug APK
./gradlew :androidApp:assembleDebug

# 构建 Release APK
./gradlew :androidApp:assembleRelease

# 清理构建
./gradlew clean

# 构建并安装到设备
./gradlew :androidApp:installDebug
```

APK 输出路径: `androidApp/build/outputs/apk/debug/` 或 `/release/`

### 测试
```bash
# 运行所有测试
./gradlew test

# 运行共享模块单元测试
./gradlew :shared:test

# 运行 Android 模块单元测试
./gradlew :androidApp:testDebugUnitTest

# 运行单个测试类
./gradlew :shared:test --tests "hua.lee.herbmind.domain.search.SearchUseCaseTest"

# 运行 UI 测试
./gradlew :androidApp:connectedDebugAndroidTest
```

### 代码生成
```bash
# SQLDelight 生成数据库代码
./gradlew generateSqlDelightInterface
```

### 检查/调试
```bash
# 查看依赖树
./gradlew dependencies

# 查看项目结构
./gradlew projects
```

## 架构说明

### 导航结构
使用 Jetpack Navigation Compose，定义在 `androidApp/src/main/kotlin/hua/lee/herbmind/android/ui/navigation/Screen.kt`:
- Home -> Search / HerbDetail / Category / Compare / Study
- Search -> HerbDetail
- HerbDetail -> FormulaDetail (通过相关方剂) / Compare (对比功能)
- FormulaDetail -> HerbDetail (通过组成药材)
- Category -> HerbDetail

### 数据同步机制
应用启动时自动从 GitHub Raw 同步数据:
1. 检查远程版本 (`resources/final_data/version.json`)
2. 远程版本 > 本地版本时同步到 SQLDelight 数据库
3. 同步内容包括：药材数据 + 方剂数据
4. 网络失败时使用本地 Assets 作为 fallback

关键类:
- `DataSyncUseCase` - 同步逻辑
- `RemoteDataSource` / `GithubRawDataSource` - 远程数据源
- `LocalDataSource` - 本地数据源
- `ResourceConfig` - 资源 URL 配置（数据源和图片源）

### 数据库结构
SQLDelight 定义在 `shared/src/commonMain/sqldelight/hua/lee/herbmind/data/Herb.sq`:
- `data_version` - 数据版本控制
- `herb` - 药材基本信息（名称、性味、功效、产地等）
- `formula` - 方剂信息（名称、组成、功用、主治等）
- `favorite` - 收藏表
- `search_history` - 搜索历史
- `browse_history` - 浏览历史
- `study_progress` - 学习进度表（V1 功能，SM2 算法）
- `review_log` - 复习日志表（V1 功能）

### 依赖注入
使用 Koin，模块定义:
- `shared/src/commonMain/kotlin/hua/lee/herbmind/di/KoinModules.kt` - 通用模块
- `androidApp/.../android/di/AppModule.kt` - Android 专用模块

### 搜索逻辑
支持多维搜索，权重如下:
- 名称(100) > 功效(40) > 主治(30) > 产地(20) > 性味(20)
- 支持同义词扩展（如"活血"匹配"化瘀、散瘀"）
- 考试频率加权排序

### 学习功能（V1 保留）
基于 SM2（SuperMemo 2）算法的间隔重复学习系统:
- 使用 `study_progress` 表记录每个药材的学习状态
- 使用 `review_log` 表记录复习历史
- 支持学习热力图展示复习记录
- WorkManager 实现每日复习提醒

### 国风设计配色
定义在 `androidApp/.../ui/theme/Color.kt`:
- 主色: 竹青 `#7CB342`
- 辅色: 赭石 `#8D6E63`
- 背景: 宣纸白 `#FAFAF8`
- 文字: 墨黑 `#2C2C2C`

## 图片资源

中药图片通过 GitHub Raw 加载:
- 饮片图: `https://raw.githubusercontent.com/hualee-auto/herbmind/main/resources/images/concocted/{拼音}.jpg`
- 植物图: `https://raw.githubusercontent.com/hualee-auto/herbmind/main/resources/images/plants/{拼音}_{编号}.jpg`

Android 平台也支持从本地 Assets 加载作为 fallback。

## 设计文档

V2 设计文档位于 `docs/v2-design/`:
- `01-PRD-HerbMind-V2.md` - 产品需求文档
- `02-UI-Visual-Design.md` - UI 视觉设计文档
- `03-Architecture-Design.md` - 架构设计文档

**开发前请先阅读设计文档。**

## 旧版本信息

V1 版本（学习功能为主）的文档保留在 `docs/` 根目录:
- `PRD-中药记忆学习App.md`
- `PRD-中药记忆学习App-技术方案-Android.md`
- `DATA_SYNC.md`

## GitHub 仓库

- **仓库**: https://github.com/hualee-auto/herbmind
- **数据源**: https://raw.githubusercontent.com/hualee-auto/herbmind/main/resources/

## 技术栈版本

- Gradle: 8.2.0
- Kotlin: 1.9.22
- Compose BOM: 2024.02.00
- Compose Compiler: 1.5.8
- Java Version: 17
- AGP: 8.2.0