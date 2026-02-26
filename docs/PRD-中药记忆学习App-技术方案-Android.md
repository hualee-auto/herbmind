# 本草记 (HerbMind) - 技术实现方案 (Android)

## 1. 技术栈选型

### 1.1 技术选型

| 维度 | 选择 | 理由 |
|-----|------|------|
| 平台 | Android | 专注 Android 平台，简化架构 |
| UI 框架 | Jetpack Compose | Google 官方推荐，声明式 UI |
| 编程语言 | Kotlin | Android 首选语言 |
| 架构 | MVVM | 响应式 UI，状态管理清晰 |
| 依赖注入 | Koin | 轻量，Kotlin 友好 |
| 本地存储 | SQLDelight | 类型安全 SQL |
| 图片加载 | Coil | Compose 原生支持 |
| 导航 | Voyager | 简洁的 Compose 导航 |

### 1.2 平台支持

仅支持 Android 平台 (minSdk: 24, targetSdk: 34)

---

## 2. 项目结构

```
herbmind/
├── build.gradle.kts              # 根构建配置
├── settings.gradle.kts
├── gradle.properties
│
├── shared/                       # 数据模块 (Android Library)
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           └── kotlin/com/herbmind/
│               ├── data/
│               │   ├── database/
│               │   │   ├── HerbDatabase.kt
│               │   │   ├── Herb.sq
│               │   │   └── DriverFactory.kt
│               │   ├── model/
│               │   │   ├── Herb.kt
│               │   │   └── SearchResult.kt
│               │   └── repository/
│               │       ├── HerbRepository.kt
│               │       └── SearchRepository.kt
│               ├── domain/
│               │   ├── search/
│               │   │   └── SearchUseCase.kt
│               │   └── recommend/
│               │       └── DailyRecommendUseCase.kt
│               └── di/
│                   └── KoinModules.kt
│
└── androidApp/                   # Android 应用模块
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── kotlin/com/herbmind/android/
        │   ├── MainActivity.kt
        │   ├── HerbMindApplication.kt
        │   └── ui/
        │       ├── theme/
        │       │   ├── Color.kt
        │       │   └── Theme.kt
        │       ├── components/
        │       ├── screens/
        │       └── navigation/
        └── res/
```

---

## 3. 核心数据结构设计

### 3.1 SQLDelight Schema

```sql
CREATE TABLE herb (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    pinyin TEXT NOT NULL,
    aliases TEXT,
    category TEXT NOT NULL,
    subCategory TEXT,
    nature TEXT,
    flavor TEXT,
    meridians TEXT,
    effects TEXT NOT NULL,
    indications TEXT,
    usage TEXT,
    contraindications TEXT,
    memoryTip TEXT,
    association TEXT,
    keyPoint TEXT,
    similarTo TEXT,
    image TEXT,
    isCommon INTEGER DEFAULT 0,
    examFrequency INTEGER DEFAULT 1
);

CREATE TABLE favorite (
    herbId TEXT PRIMARY KEY,
    addedAt INTEGER NOT NULL,
    FOREIGN KEY (herbId) REFERENCES herb(id)
);

CREATE TABLE searchHistory (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    query TEXT NOT NULL,
    timestamp INTEGER NOT NULL
);
```

### 3.2 Kotlin Data Class

```kotlin
data class Herb(
    val id: String,
    val name: String,
    val pinyin: String,
    val aliases: List<String> = emptyList(),
    val category: String,
    val subCategory: String? = null,
    val nature: String? = null,
    val flavor: List<String> = emptyList(),
    val meridians: List<String> = emptyList(),
    val effects: List<String>,
    val indications: List<String> = emptyList(),
    val usage: String? = null,
    val contraindications: List<String> = emptyList(),
    val memoryTip: String? = null,
    val association: String? = null,
    val keyPoint: String? = null,
    val similarTo: List<String> = emptyList(),
    val image: String? = null,
    val isCommon: Boolean = false,
    val examFrequency: Int = 1
)
```

---

## 4. 构建配置

### 4.1 androidApp/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "com.herbmind.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.herbmind"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":shared"))
    
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    
    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    
    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
}
```

### 4.2 shared/build.gradle.kts

```kotlin
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.sqldelight)
}

android {
    namespace = "com.herbmind"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
}

sqldelight {
    databases {
        create("HerbDatabase") {
            packageName.set("com.herbmind.data")
        }
    }
}
```

---

## 5. 开发计划

### Week 1: 基础搭建
- [x] 创建 Android 项目结构
- [x] 配置 SQLDelight 数据库
- [x] 设计数据模型
- [x] 导入初始中药数据
- [x] 配置主题和基础组件

### Week 2: 核心功能
- [ ] 实现搜索算法
- [ ] 搜索页 UI
- [ ] 搜索结果页 UI
- [ ] 中药详情页 UI
- [ ] 收藏功能

### Week 3: 推荐与浏览
- [ ] 每日推荐算法
- [ ] 首页 UI
- [ ] 分类浏览页
- [ ] 收藏列表页

### Week 4: 优化与发布
- [ ] Android 端优化
- [ ] 数据补全
- [ ] 性能优化
- [ ] 打包发布

---

## 6. 预估资源

| 资源 | 数量 |
|-----|------|
| 中药数据 | 100-300味 |
| APK 大小 | ~10-15MB |
| 运行时内存 | ~50-100MB |

---

**文档版本**: v3.0 - Android Only  
**更新日期**: 2026-02-26  
**状态**: 已简化，移除 Compose Multiplatform
