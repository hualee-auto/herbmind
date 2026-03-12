# 首页同步进度展示 Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在首页增加数据同步进度展示，让用户了解数据加载状态。

**Architecture:** 在 HomeViewModel 中监听 AppDataInitializer 的同步进度流，将进度状态添加到 HomeUiState，HomeScreen 根据状态展示同步进度卡片。

**Tech Stack:** Kotlin, Jetpack Compose, Material3, Koin DI

---

## 文件结构

- **HomeUiState** (`androidApp/.../viewmodel/HomeViewModel.kt:94-100`): 数据类，添加 syncProgress 和 syncMessage 字段
- **HomeViewModel** (`androidApp/.../viewmodel/HomeViewModel.kt`): ViewModel，添加 observeSyncProgress() 方法
- **HomeScreen** (`androidApp/.../ui/screens/HomeScreen.kt`): Composable 页面，添加 SyncProgressCard 组件
- **KoinModules** (`shared/.../di/KoinModules.kt` 或 `androidApp/.../di/AppModule.kt`): DI 模块，确保 AppDataInitializer 注入

---

## Task 1: 更新 HomeUiState 添加同步进度状态

**Files:**
- Modify: `androidApp/src/main/kotlin/com/herbmind/android/ui/viewmodel/HomeViewModel.kt:94-100`

- [ ] **Step 1: 添加同步状态字段到 HomeUiState**

```kotlin
data class HomeUiState(
    val categories: List<HerbCategory> = emptyList(),
    val recentHerbs: List<Herb> = emptyList(),
    val selectedCategory: String = "",
    val filteredHerbs: List<Herb> = emptyList(),
    val isLoading: Boolean = true,
    // 新增：同步进度状态
    val syncProgress: Int? = null,  // null 表示不显示，0-100 表示进度
    val syncMessage: String = ""    // 当前同步状态描述
)
```

- [ ] **Step 2: 提交更改**

```bash
git add androidApp/src/main/kotlin/com/herbmind/android/ui/viewmodel/HomeViewModel.kt
git commit -m "feat: HomeUiState 添加同步进度状态字段"
```

---

## Task 2: 更新 HomeViewModel 监听同步进度

**Files:**
- Modify: `androidApp/src/main/kotlin/com/herbmind/android/ui/viewmodel/HomeViewModel.kt`

- [ ] **Step 1: 修改构造函数注入 AppDataInitializer**

修改第 16-19 行：
```kotlin
class HomeViewModel(
    private val herbRepository: HerbRepository,
    private val filterUseCase: FilterHerbsUseCase,
    private val appDataInitializer: AppDataInitializer  // 新增
) : ViewModel() {
```

- [ ] **Step 2: 在 init 中添加同步进度监听**

修改第 24-26 行：
```kotlin
init {
    observeSyncProgress()  // 新增：监听同步进度
    loadData()
}
```

- [ ] **Step 3: 添加 observeSyncProgress 方法**

在第 56 行后（loadData 方法后）添加：
```kotlin
private fun observeSyncProgress() {
    viewModelScope.launch {
        appDataInitializer.initialize().collect { result ->
            when (result) {
                is com.herbmind.domain.sync.SyncResult.InProgress -> {
                    _uiState.value = _uiState.value.copy(
                        syncProgress = result.progress,
                        syncMessage = getSyncMessage(result.progress)
                    )
                }
                is com.herbmind.domain.sync.SyncResult.Success,
                is com.herbmind.domain.sync.SyncResult.NoUpdate -> {
                    _uiState.value = _uiState.value.copy(
                        syncProgress = null,
                        syncMessage = ""
                    )
                }
                is com.herbmind.domain.sync.SyncResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        syncProgress = null,
                        syncMessage = "同步失败: ${result.message}"
                    )
                }
            }
        }
    }
}

private fun getSyncMessage(progress: Int): String = when {
    progress < 15 -> "检查数据版本..."
    progress < 30 -> "获取云端数据..."
    progress < 100 -> "保存到本地数据库..."
    else -> "同步完成"
}
```

- [ ] **Step 4: 添加必要的 import**

确保文件顶部有：
```kotlin
import com.herbmind.domain.sync.AppDataInitializer
import com.herbmind.domain.sync.SyncResult
```

- [ ] **Step 5: 提交更改**

```bash
git add androidApp/src/main/kotlin/com/herbmind/android/ui/viewmodel/HomeViewModel.kt
git commit -m "feat: HomeViewModel 监听并暴露同步进度"
```

---

## Task 3: 在 HomeScreen 添加同步进度 UI

**Files:**
- Modify: `androidApp/src/main/kotlin/com/herbmind/android/ui/screens/HomeScreen.kt`

- [ ] **Step 1: 在搜索栏下方添加同步进度卡片**

在第 84-85 行之间插入：
```kotlin
// 同步进度卡片
if (uiState.syncProgress != null) {
    item {
        SyncProgressCard(
            progress = uiState.syncProgress,
            message = uiState.syncMessage
        )
    }
}
```

- [ ] **Step 2: 在文件末尾添加 SyncProgressCard 组件**

在第 325 行后添加：
```kotlin
@Composable
private fun SyncProgressCard(
    progress: Int,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = HerbColors.BambooGreen.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HerbColors.BambooGreen
                )
                Text(
                    text = "$progress%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HerbColors.BambooGreen
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = HerbColors.BambooGreen,
                trackColor = HerbColors.BambooGreen.copy(alpha = 0.2f)
            )
        }
    }
}
```

- [ ] **Step 3: 提交更改**

```bash
git add androidApp/src/main/kotlin/com/herbmind/android/ui/screens/HomeScreen.kt
git commit -m "feat: HomeScreen 添加同步进度卡片组件"
```

---

## Task 4: 更新 Koin DI 模块

**Files:**
- Modify: `androidApp/src/main/kotlin/com/herbmind/android/di/AppModule.kt` 或相关 Koin 模块文件

- [ ] **Step 1: 检查并更新 HomeViewModel 的 factory 定义**

找到 HomeViewModel 的 factory 定义，确保注入 AppDataInitializer：
```kotlin
viewModel { HomeViewModel(get(), get(), get()) }
```

如果 AppDataInitializer 未注册，需要添加：
```kotlin
single { AppDataInitializer(get()) }
```

- [ ] **Step 2: 提交更改**

```bash
git add androidApp/src/main/kotlin/com/herbmind/android/di/AppModule.kt
git commit -m "feat: 更新 DI 模块注入 AppDataInitializer"
```

---

## Task 5: 构建验证

**Files:**
- Test: 运行构建验证

- [ ] **Step 1: 运行编译**

```bash
./gradlew :androidApp:compileDebugKotlin
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 构建 APK**

```bash
./gradlew :androidApp:assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交完成标记**

```bash
git commit -m "feat: 首页同步进度展示功能完成" --allow-empty
```

---

## 验证清单

- [ ] 同步进度卡片在数据同步时显示
- [ ] 进度条实时更新（0% -> 100%）
- [ ] 显示正确的同步状态消息（检查版本/获取数据/保存数据库）
- [ ] 同步完成后进度卡片自动消失
- [ ] 使用国风配色（HerbColors.BambooGreen）
- [ ] 应用编译通过
