# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

HerbMind（本草记）— 基于 HKBU（香港浸会大学）中药材数据库的 Android 中药材查询工具。420 种药材 + 方剂，支持多维度搜索、分类浏览、数据同步、AdMob 广告。

- **包名**: `hua.lee.herbmind`
- **最低 SDK**: 24 / **目标 SDK**: 34
- **Java**: 17
- **产品定位**: 专注中药材查询工具（V2 精简版），已移除收藏/对比/学习等未实现功能

## 构建与测试

```bash
# 需要 JDK 17。CI 用的是 Gradle 8.5 wrapper
./gradlew :androidApp:assembleDebug          # Debug APK → androidApp/build/outputs/apk/debug/
./gradlew :androidApp:installDebug           # 构建并安装到设备

./gradlew :shared:test                       # 共享模块单元测试
./gradlew :androidApp:testDebugUnitTest      # Android 模块单元测试
./gradlew :shared:test --tests "hua.lee.herbmind.domain.search.SearchUseCaseTest"  # 单个测试类

./gradlew generateSqlDelightInterface        # SQLDelight 代码生成（修改 .sq 文件后必须执行）
./gradlew lint                               # Lint 检查（CI 会跑）
./gradlew koverXmlReport                     # 覆盖率报告
```

## 架构

双模块 Kotlin Multiplatform 项目，MVVM + Clean Architecture：

```
androidApp/  → Android 壳：Activity、Compose UI、ViewModel、Koin Android 模块
shared/      → 平台无关：data（Repository/Model/Remote/SQLDelight）、domain（UseCase）、di
```

**数据流**: Screen → ViewModel → UseCase → Repository → SQLDelight (本地) / Ktor (远程)

**依赖注入**: Koin。通用模块 `shared/.../di/KoinModules.kt`，Android 模块 `androidApp/.../di/AppModule.kt`。

### 数据同步

启动时 `HerbDataSyncUseCase` / `AppDataInitializer` 检查远程版本（`resources/final_data/version.json`），版本更高则同步到 SQLDelight。网络失败 fallback 到 `androidApp/src/main/assets/final_data/`。

资源 URL 通过 `ResourceConfig`（单例持有者）+ `ResourceConfigProvider`（平台实现）配置，必须在 `Application.onCreate` 中初始化。

### 搜索

`SearchUseCase` / `SearchHerbsUseCase` 实现多维度加权搜索：名称(100) > 功效(40) > 主治(30) > 产地/性味(20)。支持同义词扩展（"活血"→"化瘀、散瘀"），考试频率加权排序。

### 广告系统

`AdManager` 全局管理广告：启动预加载、30 分钟缓存刷新、多平台优先级、失败降级。`AdFrequencyController` 控制频率，`AdPlatformAdapter` 抽象平台适配（当前仅 AdMob）。广告组件在 `androidApp/.../ui/components/AdNativeCard.kt`。

### 导航

Jetpack Navigation Compose，路由定义在 `androidApp/.../ui/navigation/Screen.kt`：
Home ↔ Search ↔ HerbDetail ↔ FormulaDetail（药材/方剂双向导航）

### 数据库

SQLDelight schema：`shared/src/commonMain/sqldelight/hua/lee/herbmind/data/Herb.sq`
表：`data_version`、`herb`、`formula`、`search_history`、`browse_history`
生成代码包：`hua.lee.herbmind.data`（HerbDatabase）

## 国风设计

配色定义在 `androidApp/.../ui/theme/Color.kt`，设计 token 在 `HerbTokens.kt`：
竹青 `#7CB342` / 赭石 `#8D6E63` / 宣纸白 `#FAFAF8` / 墨黑 `#2C2C2C`

## 数据源

- 药材/方剂 JSON：`resources/final_data/`（GitHub Raw 分发）
- 本地 Assets 备份：`androidApp/src/main/assets/final_data/`
- 图片：`resources/images/concocted/{拼音}.jpg` 和 `resources/images/plants/{拼音}_{编号}.jpg`
- 爬虫脚本：`scripts/`（Python，用于从 HKBU 抓取/修复数据）

## 技术栈版本

Gradle 8.2.0 / Kotlin 1.9.22 / Compose BOM 2024.02.00 / Compose Compiler 1.5.8 / SQLDelight 2.0.1 / Koin 3.5.3 / Ktor 2.3.7 / Coil 2.5.0

## CI

GitHub Actions：`android-build.yml`（push/PR 到 main 触发构建）、`test.yml`（单元测试 + 覆盖率 + lint）

## 设计文档

V2 设计文档在 `docs/v2-design/`（PRD、UI 视觉、架构设计），开发前应先阅读。
