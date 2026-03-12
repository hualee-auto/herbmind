# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

HerbMind (本草记) 是一款基于 HKBU（香港浸会大学）中药材数据库的专业中药材查询工具。

- **平台**: Android (minSdk 24, targetSdk 34)
- **UI 框架**: Jetpack Compose
- **编程语言**: Kotlin
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Koin
- **本地存储**: SQLDelight
- **图片加载**: Coil

## 数据源

- **药材数据**: HKBU 中药材数据库，420种药材
- **方剂数据**: HKBU 中药方剂数据库
- **数据文件**: `hkbu_data/final_data/herbs_hkbu.json`
- **图片资源**: `hkbu_data/images/concocted/` (饮片图), `plants/` (植物图)

## 项目结构

```
herbmind/
├── androidApp/          # Android 应用模块
│   └── src/main/kotlin/com/herbmind/android/
│       ├── MainActivity.kt
│       ├── HerbMindApplication.kt
│       └── ui/
│           ├── theme/      # 主题配置 (国风配色)
│           ├── components/ # 可复用组件
│           ├── screens/    # 页面 (Home/Search/HerbDetail/FormulaDetail/Category)
│           ├── navigation/ # 导航定义
│           └── viewmodel/  # ViewModel
├── shared/              # Kotlin Multiplatform 共享模块
│   └── src/
│       ├── commonMain/kotlin/com/herbmind/
│       │   ├── data/       # 数据层 (Repository、Model、Remote)
│       │   │   ├── model/  # Herb, Formula 数据类
│       │   │   ├── repository/
│       │   │   ├── local/  # SQLDelight 数据库
│       │   │   └── remote/ # GitHub Raw 数据源
│       │   ├── domain/     # 领域层 (UseCase)
│       │   │   ├── search/
│       │   │   ├── herb/
│       │   │   └── formula/
│       │   └── di/         # 依赖注入配置
│       ├── androidMain/    # Android 平台特定实现
│       └── commonTest/     # 单元测试
├── hkbu_data/           # HKBU 数据
│   ├── final_data/       # JSON 数据文件
│   └── images/           # 图片资源
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

# 清理构建
./gradlew clean

# 构建并安装到设备
./gradlew :androidApp:installDebug
```

APK 输出路径: `androidApp/build/outputs/apk/debug/`

### 测试
```bash
# 运行所有测试
./gradlew test

# 运行共享模块单元测试
./gradlew :shared:test

# 运行 Android 模块单元测试
./gradlew :androidApp:testDebugUnitTest
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
使用 Jetpack Navigation Compose，定义在 `androidApp/src/main/kotlin/com/herbmind/android/ui/navigation/Screen.kt`:
- Home -> Search / HerbDetail / Category
- HerbDetail -> FormulaDetail (通过相关方剂)
- FormulaDetail -> HerbDetail (通过组成药材)
- Search -> HerbDetail

### 数据同步机制
应用启动时自动从 GitHub Raw 同步数据:
1. 检查远程版本 (`hkbu_data/final_data/version.json`)
2. 远程版本 > 本地版本时同步到 SQLDelight 数据库
3. 同步内容包括：药材数据 + 方剂数据
4. 网络失败时使用本地 Assets 作为 fallback

关键类:
- `DataSyncUseCase` - 同步逻辑
- `RemoteDataSource` / `GithubRawDataSource` - 远程数据源
- `LocalDataSource` - 本地数据源

### 数据库结构
SQLDelight 定义在 `shared/src/commonMain/sqldelight/com/herbmind/data/Herb.sq`:
- `herb` - 药材基本信息（名称、性味、功效、产地等）
- `formula` - 方剂信息（名称、组成、功用、主治等）
- `search_history` - 搜索历史
- `browse_history` - 浏览历史
- `data_version` - 数据版本控制

### 依赖注入
使用 Koin，模块定义:
- `shared/src/commonMain/kotlin/com/herbmind/di/KoinModules.kt` - 通用模块
- `androidApp/.../android/di/AppModule.kt` - Android 专用模块

### 搜索逻辑
支持多维搜索:
- **名称搜索**: 中文名、拼音、拉丁名、别名
- **功效搜索**: 功效关键词（支持同义词扩展）
- **主治搜索**: 主治病症
- **产地搜索**: 省份/地区
- **性味搜索**: 四气五味

搜索权重: 名称(100) > 功效(40) > 主治(30) > 产地(20) > 性味(20)

### 国风设计配色
定义在 `androidApp/.../ui/theme/Color.kt`:
- 主色: 竹青 `#7CB342`
- 辅色: 赭石 `#8D6E63`
- 背景: 宣纸白 `#FAFAF8`
- 文字: 墨黑 `#2C2C2C`

## 图片资源

中药图片存储在 `hkbu_data/images/`，命名格式: `{药材拼音小写}.jpg`
- 饮片图: `concocted/badou.jpg`
- 植物图: `plants/badou_D01391.jpg`

图片通过本地 Assets 或 CDN 加载。

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
