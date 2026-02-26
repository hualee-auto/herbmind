# 本草记 HerbMind

一款基于 Compose Multiplatform 开发的中药查询学习应用。

## 功能特性

- **功效反查中药**：输入功效关键词，智能匹配相关中药
- **每日推荐**：根据节气、考试频率、易混淆药等维度推荐学习内容
- **中药库浏览**：按分类浏览全部中药
- **收藏功能**：收藏常用中药，方便复习
- **记忆辅助**：记忆口诀、趣味联想，帮助记忆

## 技术栈

- **UI 框架**: Compose Multiplatform
- **编程语言**: Kotlin
- **架构**: MVVM + MVI
- **依赖注入**: Koin
- **本地存储**: SQLDelight
- **导航**: Voyager

## 项目结构

```
herbmind/
├── shared/              # 共享模块 (KMP)
│   ├── data/           # 数据层 (数据库、模型、仓库)
│   ├── domain/         # 领域层 (UseCase)
│   └── di/             # 依赖注入配置
├── composeApp/         # Compose UI
│   ├── theme/          # 主题配置 (国风配色)
│   ├── components/     # 可复用组件
│   ├── screens/        # 页面
│   └── navigation/     # 导航
└── androidApp/         # Android 入口
```

## 国风设计

- **主色**: 竹青 (#7CB342)
- **辅色**: 赭石 (#8D6E63)
- **背景**: 宣纸白 (#FAFAF8)
- **文字**: 墨黑 (#2C2C2C)

## 构建运行

### Android

```bash
./gradlew :androidApp:installDebug
```

### iOS

```bash
./gradlew :composeApp:podInstall
# 然后在 Xcode 中打开 iosApp 运行
```

## 数据

包含 30+ 味常用中药的完整数据，包括：
- 性味归经
- 功效主治
- 记忆口诀
- 趣味联想
- 易混淆对比

## 核心算法

### 功效搜索
- 支持同义词扩展（如"活血"匹配"化瘀、散瘀"）
- 多维度评分（功效匹配、名称匹配、主治匹配）
- 考试频率加权

### 每日推荐
- 节气推荐（根据当前季节）
- 高频考点（考试频率加权随机）
- 易混淆药（有相似药物的药材）

## 截图

*(待添加)*

## 许可证

MIT License
# Build Status
