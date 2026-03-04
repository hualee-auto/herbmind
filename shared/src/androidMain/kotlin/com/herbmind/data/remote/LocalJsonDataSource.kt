package com.herbmind.data.remote

import android.content.Context
import com.herbmind.data.model.Herb
import com.herbmind.domain.sync.DataVersionInfo
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * 本地 JSON 数据源
 * 
 * 从 Android assets 中读取本地 JSON 文件
 * 用于首次启动或远程获取失败时的 fallback
 */
class LocalJsonDataSource(
    private val context: Context,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : HerbRemoteDataSource {

    override suspend fun getVersionInfo(): Result<DataVersionInfo> {
        return try {
            val jsonString = context.assets.open("final_data/version.json").use { 
                it.bufferedReader().readText() 
            }
            val versionInfo = json.decodeFromString(DataVersionInfo.serializer(), jsonString)
            Result.success(versionInfo)
        } catch (e: Exception) {
            Result.failure(Exception("读取本地版本信息失败: ${e.message}"))
        }
    }

    override suspend fun getHerbData(): Result<List<Herb>> {
        return try {
            val jsonString = context.assets.open("final_data/herbs_split.json").use { 
                it.bufferedReader().readText() 
            }
            val herbs = json.decodeFromString<List<Herb>>(jsonString)
            Result.success(herbs)
        } catch (e: Exception) {
            Result.failure(Exception("读取本地中药数据失败: ${e.message}"))
        }
    }

    override fun getImageUrl(halfPath: String): String {
        // 本地图片从 assets 加载
        return "file:///android_asset/$halfPath"
    }
}
