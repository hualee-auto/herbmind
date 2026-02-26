# 项目变更说明

## 2026-02-26 技术方案调整

### 变更内容

**原方案**: Compose Multiplatform (Android + iOS + Desktop)
**新方案**: 纯 Android (Jetpack Compose)

### 变更原因

1. **简化架构**: 移除跨平台复杂性，专注 Android 平台
2. **减少依赖问题**: Compose Multiplatform 版本兼容性复杂
3. **加快开发速度**: 单平台开发更高效
4. **降低维护成本**: 无需处理多平台差异

### 具体变更

#### 1. 移除的模块
- ~~`composeApp`~~ (Compose Multiplatform 共享模块)
- ~~iOS 支持~~
- ~~Desktop 支持~~

#### 2. 保留的模块
- `androidApp` (Android 应用模块)
- `shared` (数据层模块，改为 Android Library)

#### 3. 技术栈变更

| 组件 | 原方案 | 新方案 |
|------|--------|--------|
| UI 框架 | Compose Multiplatform | Jetpack Compose |
| 项目结构 | KMP (Kotlin Multiplatform) | 纯 Android |
| 依赖注入 | Koin (多平台) | Koin (Android) |
| 图片加载 | Coil 3 (多平台) | Coil 2 (Android) |
| 导航 | Voyager (多平台) | Voyager (Android) |

#### 4. 构建配置变更

**原配置**:
```kotlin
// Compose Multiplatform
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidApplication)
}
```

**新配置**:
```kotlin
// Android Only
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}
```

#### 5. 包结构变更

**原结构**:
```
shared/src/
  ├── commonMain/kotlin/ (通用代码)
  ├── androidMain/kotlin/ (Android 特定)
  └── iosMain/kotlin/ (iOS 特定)
```

**新结构**:
```
shared/src/
  └── main/kotlin/com/herbmind/ (Android Library)
```

### 文档更新

1. ✅ 创建新的技术方案文档: `PRD-中药记忆学习App-技术方案-Android.md`
2. ✅ UI 设计规范保持不变 (设计是平台无关的)
3. ✅ UI 原型保持不变 (界面设计不变)

### GitHub 仓库

- 仓库地址: https://github.com/hualee-auto/herbmind
- 构建状态: ✅ 成功
- APK 产物: ✅ 已生成

### 后续计划

1. 完善 Android 应用功能
2. 添加完整的 UI 实现
3. 集成数据层
4. 测试和优化

---

**更新日期**: 2026-02-26  
**更新人**: HerbMind Developer
