package hua.lee.herbmind.domain.sync

import hua.lee.herbmind.data.HerbDatabase
import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.data.model.Images
import hua.lee.herbmind.data.remote.HerbRemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * 数据版本信息
 */
@Serializable
data class DataVersionInfo(
    val version: Int,
    val lastUpdated: Long,
    val herbCount: Int,
    val formulaCount: Int = 0,
    val description: String? = null,
    val minAppVersion: String? = null
)

/**
 * 数据同步结果
 */
sealed class SyncResult {
    data class Success(
        val newVersion: Int,
        val syncedHerbs: Int,
        val isFirstSync: Boolean
    ) : SyncResult()

    data class NoUpdate(val currentVersion: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
    data class InProgress(val progress: Int) : SyncResult()
}

/**
 * 中药数据同步 UseCase
 *
 * 实现云端数据版本检查和同步逻辑：
 * 1. 应用启动时检查云端版本号
 * 2. 比较本地版本与云端版本
 * 3. 如果本地版本 < 云端版本，下载并同步数据
 * 4. 如果远程获取失败，使用本地 JSON 作为 fallback
 */
class HerbDataSyncUseCase(
    private val database: HerbDatabase,
    private val remoteDataSource: HerbRemoteDataSource,
    private val localDataSource: HerbRemoteDataSource? = null,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    private val queries = database.herbQueries

    /**
     * 获取当前本地数据版本
     */
    fun getLocalVersion(): Flow<Int> = flow {
        try {
            val version = queries.selectDataVersion().executeAsOneOrNull()
            emit(version?.version?.toInt() ?: 0)
        } catch (e: Exception) {
            emit(0)
        }
    }.flowOn(Dispatchers.Default)

    /**
     * 检查是否需要同步
     * @param remoteVersion 云端版本号
     * @return true 需要同步，false 不需要
     */
    fun shouldSync(remoteVersion: Int): Flow<Boolean> = flow {
        try {
            val localVersion = queries.selectDataVersion().executeAsOneOrNull()
            val currentVersion = localVersion?.version?.toInt() ?: 0
            emit(currentVersion < remoteVersion)
        } catch (e: Exception) {
            // 如果表不存在或出错，认为需要同步
            emit(true)
        }
    }.flowOn(Dispatchers.Default)

    /**
     * 执行数据同步
     * @param remoteHerbs 从云端获取的中药数据列表
     * @param remoteVersion 云端版本号
     * @return 同步结果
     */
    fun syncHerbData(
        remoteHerbs: List<Herb>,
        remoteVersion: Int
    ): Flow<SyncResult> = flow {
        try {
            // 获取当前本地版本
            val localVersionRow = queries.selectDataVersion().executeAsOneOrNull()
            val localVersion = localVersionRow?.version?.toInt() ?: 0

            // 检查是否需要同步
            if (localVersion >= remoteVersion) {
                emit(SyncResult.NoUpdate(localVersion))
                return@flow
            }

            val isFirstSync = localVersion == 0
            val totalHerbs = remoteHerbs.size
            var syncedCount = 0

            emit(SyncResult.InProgress(0))

            // 批量插入/更新中药数据
            remoteHerbs.forEachIndexed { index, herb ->
                insertOrUpdateHerb(herb)
                syncedCount++

                // 每10条发送一次进度
                if (index % 10 == 0 || index == totalHerbs - 1) {
                    val progress = ((index + 1) * 100 / totalHerbs)
                    emit(SyncResult.InProgress(progress))
                }
            }

            // 更新版本号
            val now = Clock.System.now().toEpochMilliseconds()
            queries.updateDataVersion(
                version = remoteVersion.toLong(),
                lastSyncAt = now,
                herbCount = remoteHerbs.size.toLong(),
                formulaCount = 0
            )

            emit(SyncResult.Success(
                newVersion = remoteVersion,
                syncedHerbs = syncedCount,
                isFirstSync = isFirstSync
            ))

        } catch (e: Exception) {
            emit(SyncResult.Error("同步失败: ${e.message}"))
        }
    }.flowOn(Dispatchers.Default)

    /**
     * 从远程数据源获取版本信息
     */
    suspend fun fetchRemoteVersionInfo(): Result<DataVersionInfo> {
        return remoteDataSource.getVersionInfo()
    }

    /**
     * 从远程数据源获取中药数据
     */
    suspend fun fetchRemoteHerbData(): Result<List<Herb>> {
        return remoteDataSource.getHerbData()
    }

    /**
     * 从本地数据源获取版本信息（fallback）
     */
    suspend fun fetchLocalVersionInfo(): Result<DataVersionInfo>? {
        return localDataSource?.getVersionInfo()
    }

    /**
     * 从本地数据源获取中药数据（fallback）
     */
    suspend fun fetchLocalHerbData(): Result<List<Herb>>? {
        return localDataSource?.getHerbData()
    }

    /**
     * 检查并执行同步（完整流程）
     *
     * 仅从远程获取数据，本地 assets 已移除
     *
     * @return 同步结果流
     */
    fun checkAndSync(): Flow<SyncResult> = flow {
        try {
            // 1. 获取云端版本信息
            val versionInfoResult = fetchRemoteVersionInfo()

            if (versionInfoResult.isFailure) {
                emit(SyncResult.Error("获取版本信息失败: ${versionInfoResult.exceptionOrNull()?.message}"))
                return@flow
            }

            val versionInfo = versionInfoResult.getOrThrow()
            val remoteVersion = versionInfo.version

            // 2. 检查是否需要同步
            val localVersion = queries.selectDataVersion().executeAsOneOrNull()?.version?.toInt() ?: 0

            if (localVersion >= remoteVersion) {
                emit(SyncResult.NoUpdate(localVersion))
                return@flow
            }

            // 3. 获取中药数据
            val herbDataResult = fetchRemoteHerbData()

            if (herbDataResult.isFailure) {
                emit(SyncResult.Error("获取中药数据失败: ${herbDataResult.exceptionOrNull()?.message}"))
                return@flow
            }

            val remoteHerbs = herbDataResult.getOrThrow()

            // 4. 执行同步
            syncHerbData(remoteHerbs, remoteVersion).collect { result ->
                emit(result)
            }

        } catch (e: Exception) {
            emit(SyncResult.Error("同步流程出错: ${e.message}"))
        }
    }.flowOn(Dispatchers.Default)

    /**
     * 从本地 JSON 文件同步（用于首次启动或强制刷新）
     */
    fun syncFromLocalJson(
        herbs: List<Herb>,
        version: Int
    ): Flow<SyncResult> = syncHerbData(herbs, version)

    // ========== 私有方法 ==========

    private inline fun <reified T> encodeList(list: List<T>): String {
        return json.encodeToString(ListSerializer(serializer<T>()), list)
    }

    private fun insertOrUpdateHerb(herb: Herb) {
        // V2: 使用新的表结构，使用 INSERT OR REPLACE
        // 收藏数据通过独立的 favorite 表管理，不会被级联删除
        queries.insertHerb(
            id = herb.id,
            name = herb.name,
            pinyin = herb.pinyin,
            latin_name = herb.latinName,
            aliases = encodeList(herb.aliases),
            category = herb.category,
            nature = herb.nature,
            flavor = encodeList(herb.flavor),
            meridians = encodeList(herb.meridians),
            effects = encodeList(herb.effects),
            indications = encodeList(herb.indications),
            origin = herb.origin,
            traits = herb.traits,
            quality = herb.quality,
            images = json.encodeToString(Images.serializer(), herb.images),
            source_url = herb.sourceUrl,
            related_formulas = encodeList(herb.relatedFormulas)
        )
    }
}
