package com.herbmind.android.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.herbmind.data.HerbDatabase
import kotlinx.datetime.Clock

/**
 * 初始化学习记录 Worker
 * 将现有药材添加到学习系统（默认设置为首次学习状态）
 */
class StudyDataInitializer(
    context: Context,
    params: WorkerParameters,
    private val database: HerbDatabase
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "study_data_initializer"
    }

    override suspend fun doWork(): Result {
        return try {
            val queries = database.herbQueries
            val now = Clock.System.now().toEpochMilliseconds()
            
            // 获取所有药材
            val allHerbs = queries.selectAll().executeAsList()
            
            // 为每味药材创建学习记录（如果还没有）
            allHerbs.forEach { herb ->
                val existing = queries.selectStudyProgress(herb.id).executeAsOneOrNull()
                if (existing == null) {
                    // 首次学习：设置10分钟后第一次复习
                    val nextReview = now + (10 * 60 * 1000L) // 10分钟后
                    queries.startStudying(herb.id, now, nextReview)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

/**
 * 学习数据初始化工具
 */
object StudyInitializer {
    
    /**
     * 检查是否已初始化
     */
    fun isInitialized(context: Context): Boolean {
        val prefs = context.getSharedPreferences("herbmind_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("study_initialized", false)
    }
    
    /**
     * 标记已初始化
     */
    fun markInitialized(context: Context) {
        context.getSharedPreferences("herbmind_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("study_initialized", true)
            .apply()
    }
}