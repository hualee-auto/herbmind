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