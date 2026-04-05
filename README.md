# 本草记 HerbMind

一款基于 Jetpack Compose 开发的 Android 中药材查询工具，基于香港浸会大学（HKBU）中药材数据库，包含420种常见中药材和方剂信息。

## 功能特性

- **多维度搜索**：支持药材名称、拼音、拉丁名、别名、功效、主治、产地、性味搜索
- **方剂查询**：支持方剂信息查询及与药材的关联导航
- **分类浏览**：按药材类别、产地、性味、归经、功效类别进行筛选
- **常用功效快捷入口**：提供"补气、补血、活血、清热、祛湿、止咳"等常用功效一键搜索
- **数据同步**：应用启动时自动从 GitHub Raw 同步最新数据
- **离线可用**：基础数据完全离线可用，图片支持本地缓存
- **国风设计**：采用竹青、赭石、宣纸白、墨黑的传统中医配色方案

## 技术栈

- **平台**: Android (minSdk 24, targetSdk 34)
- **UI 框架**: Jetpack Compose
- **编程语言**: Kotlin 1.9.22
- **架构**: MVVM + Clean Architecture
- **依赖注入**: Koin 3.5.3
- **本地存储**: SQLDelight 2.0.1
- **图片加载**: Coil 2.5.0
- **网络请求**: Ktor 2.3.7
- **包名**: `hua.lee.herbmind`（已从 com.herbmind 迁移）

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
└── docs/                # 项目文档
    ├── v2-design/      # V2 设计文档
    │   ├── 01-PRD-HerbMind-V2.md
    │   ├── 02-UI-Visual-Design.md
    │   └── 03-Architecture-Design.md
    ├── privacy-policy.md # 隐私政策索引
    ├── privacy-policy-zh.md # 中文隐私政策
    ├── privacy-policy-en.md # 英文隐私政策
    ├── PRD-中药记忆学习App.md # V1 产品需求文档
    ├── PRD-中药记忆学习App-技术方案-Android.md # V1 技术方案
    └── CHANGELOG.md
```

## 国风设计

- **主色**: 竹青 (#7CB342)
- **辅色**: 赭石 (#8D6E63)
- **背景**: 宣纸白 (#FAFAF8)
- **文字**: 墨黑 (#2C2C2C)

## 构建运行

### 常用命令

```bash
# 构建 Debug APK
./gradlew :androidApp:assembleDebug

# 构建并安装到设备
./gradlew :androidApp:installDebug

# 构建 Release APK
./gradlew :androidApp:assembleRelease

# 运行所有测试
./gradlew test

# SQLDelight 生成数据库代码
./gradlew generateSqlDelightInterface
```

APK 输出路径: `androidApp/build/outputs/apk/debug/` 或 `/release/`

## 数据源

- **药材数据**: HKBU 中药材数据库，420种药材
- **方剂数据**: HKBU 中药方剂数据库
- **数据文件**: `resources/final_data/`（GitHub Raw）
- **本地备份**: `androidApp/src/main/assets/final_data/`
- **图片资源**: 饮片图 (`resources/images/concocted/`)、植物图 (`resources/images/plants/`)

## 核心机制

### 搜索逻辑
- 多维度权重：名称(100) > 功效(40) > 主治(30) > 产地(20) > 性味(20)
- 支持同义词扩展（如"活血"匹配"化瘀、散瘀"）
- 考试频率加权排序

### 数据同步
- 应用启动时自动检查远程数据版本
- 远程版本更新时自动同步到本地数据库
- 网络失败时使用本地Assets数据作为 fallback

### 导航结构
- Home -> Search / HerbDetail
- Search -> HerbDetail
- HerbDetail -> FormulaDetail
- FormulaDetail -> HerbDetail
- Category -> HerbDetail

## 文档

| 文档 | 说明 |
|------|------|
| [隐私政策](docs/privacy-policy.md) | 中英文隐私政策 |

## 构建状态

[![Android Build](https://github.com/hualee-auto/herbmind/actions/workflows/android-build.yml/badge.svg)](https://github.com/hualee-auto/herbmind/actions)

## 许可证

MIT License
