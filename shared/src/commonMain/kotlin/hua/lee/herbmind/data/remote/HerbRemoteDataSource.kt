package hua.lee.herbmind.data.remote

import hua.lee.herbmind.data.model.Herb
import hua.lee.herbmind.domain.sync.DataVersionInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

/**
 * 中药数据远程数据源接口
 */
interface HerbRemoteDataSource {

    /**
     * 获取云端版本信息
     * @return 版本信息或错误
     */
    suspend fun getVersionInfo(): Result<DataVersionInfo>

    /**
     * 获取云端中药数据
     * @return 中药数据列表或错误
     */
    suspend fun getHerbData(): Result<List<Herb>>

    /**
     * 获取图片的远程 URL
     * @param halfPath 图片半路径
     * @return 完整图片 URL
     */
    fun getImageUrl(halfPath: String): String
}

/**
 * GitHub Raw 远程数据源实现
 * 
 * 注意：GitHub Raw 返回 text/plain 类型，需要手动解析 JSON
 * 
 * 使用 ResourceConfig 获取基础 URL，支持国内/海外 CDN 切换
 */
class GitHubRawDataSource(
    private val versionInfoPath: String = "version.json",
    private val herbDataPath: String = "herbs_split.json",
    private val json: Json = Json { ignoreUnknownKeys = true }
) : HerbRemoteDataSource {

    private val client = HttpClient()

    override suspend fun getVersionInfo(): Result<DataVersionInfo> {
        return try {
            val url = ResourceConfig.getDataBaseUrl() + versionInfoPath
            val responseText = client.get(url).bodyAsText()
            val versionInfo = json.decodeFromString(DataVersionInfo.serializer(), responseText)
            Result.success(versionInfo)
        } catch (e: ClientRequestException) {
            Result.failure(Exception("获取版本信息失败: HTTP ${e.response.status.value}"))
        } catch (e: Exception) {
            Result.failure(Exception("获取版本信息失败: ${e.message}"))
        }
    }

    override suspend fun getHerbData(): Result<List<Herb>> {
        return try {
            val url = ResourceConfig.getDataBaseUrl() + herbDataPath
            val responseText = client.get(url).bodyAsText()
            val herbs = json.decodeFromString<List<Herb>>(responseText)
            Result.success(herbs)
        } catch (e: ClientRequestException) {
            Result.failure(Exception("获取中药数据失败: HTTP ${e.response.status.value}"))
        } catch (e: Exception) {
            Result.failure(Exception("获取中药数据失败: ${e.message}"))
        }
    }

    override fun getImageUrl(halfPath: String): String {
        // 如果已经是完整 URL，直接返回
        if (halfPath.startsWith("http://") || halfPath.startsWith("https://")) {
            return halfPath
        }
        // 使用 ResourceConfig 获取图片基础 URL
        return ResourceConfig.getImageBaseUrl() + halfPath
    }
}

/**
 * 模拟远程数据源（用于测试）
 */
class MockRemoteDataSource(
    private val mockVersion: DataVersionInfo,
    private val mockHerbs: List<Herb>
) : HerbRemoteDataSource {

    override suspend fun getVersionInfo(): Result<DataVersionInfo> {
        return Result.success(mockVersion)
    }

    override suspend fun getHerbData(): Result<List<Herb>> {
        return Result.success(mockHerbs)
    }

    override fun getImageUrl(halfPath: String): String {
        return "file:///mock/$halfPath"
    }
}
