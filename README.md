# 本草记 HerbMind

一款基于 Jetpack Compose 开发的 Android 中药查询学习应用。

## 功能特性

- **功效反查中药**：输入功效关键词，智能匹配相关中药
- **每日推荐**：根据节气、考试频率、易混淆药等维度推荐学习内容
- **中药库浏览**：按分类浏览全部中药
- **收藏功能**：收藏常用中药，方便复习
- **记忆辅助**：记忆口诀、趣味联想，帮助记忆

## 技术栈

- **平台**: Android (minSdk 24, targetSdk 34)
- **UI 框架**: Jetpack Compose
- **编程语言**: Kotlin
- **架构**: MVVM
- **本地存储**: SQLDelight
- **图片加载**: Coil

## 项目结构

```
herbmind/
├── androidApp/          # Android 应用模块
│   ├── src/main/kotlin/com/herbmind/android/
│   │   ├── MainActivity.kt
│   │   ├── HerbMindApplication.kt
│   │   └── ui/
│   │       ├── theme/      # 主题配置 (国风配色)
│   │       ├── components/ # 可复用组件
│   │       └── screens/    # 页面
│   └── build.gradle.kts
├── shared/              # 数据模块 (Android Library)
│   ├── src/main/kotlin/com/herbmind/
│   │   ├── data/         # 数据层 (数据库、模型、仓库)
│   │   ├── domain/       # 领域层 (UseCase)
│   │   └── di/           # 依赖注入配置
│   └── build.gradle.kts
└── docs/                # 项目文档
    ├── PRD-中药记忆学习App.md
    ├── PRD-中药记忆学习App-UI原型-v2.md
    ├── PRD-中药记忆学习App-技术方案-Android.md
    ├── design.md
    ├── UI-Visual-Design.md
    └── CHANGELOG.md
```

## 国风设计

- **主色**: 竹青 (#7CB342)
- **辅色**: 赭石 (#8D6E63)
- **背景**: 宣纸白 (#FAFAF8)
- **文字**: 墨黑 (#2C2C2C)

## 构建运行

### Android

```bash
./gradlew :androidApp:assembleDebug
```

APK 将生成在 `androidApp/build/outputs/apk/debug/`

## 文档

项目文档位于 `docs/` 目录：

| 文档 | 说明 |
|------|------|
| [PRD-中药记忆学习App.md](docs/PRD-中药记忆学习App.md) | 产品需求文档 |
| [PRD-中药记忆学习App-UI原型-v2.md](docs/PRD-中药记忆学习App-UI原型-v2.md) | UI 原型设计 |
| [PRD-中药记忆学习App-技术方案-Android.md](docs/PRD-中药记忆学习App-技术方案-Android.md) | Android 技术方案 |
| [design.md](docs/design.md) | UI 设计规范 |
| [UI-Visual-Design.md](docs/UI-Visual-Design.md) | UI 视觉设计图 |
| [CHANGELOG.md](docs/CHANGELOG.md) | 项目变更记录 |

## 核心算法

### 功效搜索
- 支持同义词扩展（如"活血"匹配"化瘀、散瘀"）
- 多维度评分（功效匹配、名称匹配、主治匹配）
- 考试频率加权

### 每日推荐
- 节气推荐（根据当前季节）
- 高频考点（考试频率加权随机）
- 易混淆药（有相似药物的药材）

## 构建状态

[![Android Build](https://github.com/hualee-auto/herbmind/actions/workflows/android-build.yml/badge.svg)](https://github.com/hualee-auto/herbmind/actions)

## 许可证

MIT License
