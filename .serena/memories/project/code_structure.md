```
herbmind/
├── androidApp/          # Android 应用模块
│   └── src/main/kotlin/hua/lee/herbmind/android/
│       ├── MainActivity.kt
│       ├── HerbMindApplication.kt
│       └── ui/
│           ├── theme/      # 主题配置
│           ├── components/ # 可复用组件
│           ├── screens/    # 页面
│           ├── navigation/ # 导航定义
│           └── viewmodel/  # ViewModel
├── shared/              # Kotlin Multiplatform 共享模块
│   └── src/
│       ├── commonMain/kotlin/hua/lee/herbmind/
│       │   ├── data/       # 数据层
│       │   ├── domain/     # 领域层
│       │   └── di/         # 依赖注入配置
│       ├── androidMain/    # Android 平台特定实现
│       └── commonTest/     # 单元测试
├── resources/            # 资源文件
└── docs/                # 文档
```