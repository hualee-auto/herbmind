package hua.lee.herbmind.domain.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 应用数据初始化管理器
 * 
 * 在应用启动时执行：
 * 1. 检查云端版本
 * 2. 比较本地版本
 * 3. 如果需要则同步数据
 * 4. 支持本地 fallback
 */
class AppDataInitializer(
    private val herbDataSyncUseCase: HerbDataSyncUseCase
) {
    /**
     * 执行初始化检查
     * 
     * 流程：
     * 1. 获取云端版本信息（仅从远程 GitHub）
     * 2. 比较版本号
     * 3. 如果需要更新，执行数据同步
     * 
     * 注意：本地 assets 数据已移除，完全依赖云端
     * 
     * @return 初始化结果流
     */
    fun initialize(): Flow<SyncResult> = flow {
        emit(SyncResult.InProgress(0))
        
        // 1. 获取版本信息（仅从远程）
        emit(SyncResult.InProgress(5))
        val versionInfoResult = herbDataSyncUseCase.fetchRemoteVersionInfo()
        
        if (versionInfoResult.isFailure) {
            emit(SyncResult.Error("获取版本信息失败: ${versionInfoResult.exceptionOrNull()?.message}"))
            return@flow
        }
        
        val versionInfo = versionInfoResult.getOrThrow()
        val remoteVersion = versionInfo.version
        
        emit(SyncResult.InProgress(15))
        
        // 2. 检查是否需要同步
        val localVersion = herbDataSyncUseCase.getLocalVersion()
        var currentLocalVersion = 0
        localVersion.collect { currentLocalVersion = it }
        
        if (currentLocalVersion >= remoteVersion) {
            emit(SyncResult.NoUpdate(currentLocalVersion))
            return@flow
        }
        
        // 3. 获取中药数据（仅从远程）
        emit(SyncResult.InProgress(20))
        val herbDataResult = herbDataSyncUseCase.fetchRemoteHerbData()
        
        if (herbDataResult.isFailure) {
            emit(SyncResult.Error("获取中药数据失败: ${herbDataResult.exceptionOrNull()?.message}"))
            return@flow
        }
        
        val remoteHerbs = herbDataResult.getOrThrow()
        
        // 4. 执行数据同步
        herbDataSyncUseCase.syncHerbData(remoteHerbs, remoteVersion).collect { result ->
            // 将进度从 30-100 映射
            when (result) {
                is SyncResult.InProgress -> {
                    val adjustedProgress = 30 + (result.progress * 70 / 100)
                    emit(SyncResult.InProgress(adjustedProgress))
                }
                else -> emit(result)
            }
        }
    }
    
    /**
     * 仅检查是否需要更新，不执行同步（仅从远程检查）
     * 
     * @return true 需要更新，false 不需要
     */
    suspend fun checkUpdateNeeded(): Boolean {
        // 检查远程版本
        val remoteResult = herbDataSyncUseCase.fetchRemoteVersionInfo()
        
        if (remoteResult.isSuccess) {
            val remoteVersion = remoteResult.getOrThrow().version
            val shouldSync = herbDataSyncUseCase.shouldSync(remoteVersion)
            
            // 收集 Flow 结果
            var result = false
            shouldSync.collect { result = it }
            return result
        }
        
        return false
    }
    
    /**
     * 获取远程数据版本信息
     */
    suspend fun getRemoteVersionInfo(): DataVersionInfo? {
        val remoteResult = herbDataSyncUseCase.fetchRemoteVersionInfo()
        if (remoteResult.isSuccess) {
            return remoteResult.getOrThrow()
        }
        return null
    }
}
