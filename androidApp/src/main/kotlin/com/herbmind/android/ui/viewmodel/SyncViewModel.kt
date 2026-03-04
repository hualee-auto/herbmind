package com.herbmind.android.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.herbmind.domain.sync.AppDataInitializer
import com.herbmind.domain.sync.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 数据同步状态
 */
sealed class SyncUiState {
    object Idle : SyncUiState()
    object Checking : SyncUiState()
    data class Progress(val percent: Int, val message: String) : SyncUiState()
    data class Success(val version: Int, val herbCount: Int, val isFirstSync: Boolean) : SyncUiState()
    object NoUpdate : SyncUiState()
    data class Error(val message: String) : SyncUiState()
}

/**
 * 数据同步 ViewModel
 * 
 * 管理应用启动时的数据同步状态和进度展示
 */
class SyncViewModel(
    private val appDataInitializer: AppDataInitializer
) : ViewModel() {

    companion object {
        private const val TAG = "SyncViewModel"
    }

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState

    /**
     * 开始数据同步
     */
    fun startSync() {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Checking
            
            try {
                appDataInitializer.initialize().collectLatest { result ->
                    when (result) {
                        is SyncResult.InProgress -> {
                            val message = when {
                                result.progress == 0 -> "正在检查数据版本..."
                                result.progress < 30 -> "正在下载数据..."
                                result.progress < 60 -> "正在解析数据..."
                                result.progress < 90 -> "正在保存到本地..."
                                else -> "即将完成..."
                            }
                            _syncState.value = SyncUiState.Progress(result.progress, message)
                            Log.d(TAG, "同步进度: ${result.progress}%")
                        }
                        is SyncResult.Success -> {
                            _syncState.value = SyncUiState.Success(
                                version = result.newVersion,
                                herbCount = result.syncedHerbs,
                                isFirstSync = result.isFirstSync
                            )
                            Log.i(TAG, "同步完成: 版本 ${result.newVersion}, ${result.syncedHerbs} 味药材")
                        }
                        is SyncResult.NoUpdate -> {
                            _syncState.value = SyncUiState.NoUpdate
                            Log.i(TAG, "数据已是最新，无需同步")
                        }
                        is SyncResult.Error -> {
                            _syncState.value = SyncUiState.Error(result.message)
                            Log.e(TAG, "同步失败: ${result.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                _syncState.value = SyncUiState.Error("同步过程异常: ${e.message}")
                Log.e(TAG, "同步异常", e)
            }
        }
    }

    /**
     * 标记同步完成（隐藏进度界面）
     */
    fun dismissSync() {
        _syncState.value = SyncUiState.Idle
    }

    /**
     * 重试同步
     */
    fun retrySync() {
        startSync()
    }
}
