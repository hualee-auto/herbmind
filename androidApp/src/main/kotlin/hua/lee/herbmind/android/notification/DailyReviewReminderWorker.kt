package hua.lee.herbmind.android.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import hua.lee.herbmind.android.MainActivity
import hua.lee.herbmind.android.R
import hua.lee.herbmind.data.HerbDatabase
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import java.util.concurrent.TimeUnit

/**
 * 每日复习提醒 Worker
 */
class DailyReviewReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val database: HerbDatabase
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "daily_review_reminder"
        const val CHANNEL_ID = "herbmind_review_channel"
        const val CHANNEL_NAME = "复习提醒"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        return try {
            // 查询今日待复习数量
            val queries = database.herbQueries
            val now = Clock.System.now().toEpochMilliseconds()
            val tomorrow = now + (24 * 60 * 60 * 1000L)

            // 使用 try-catch 包装数据库查询，避免表不存在时崩溃
            val dueCount = try {
                queries.selectDueToday(tomorrow).executeAsList().size
            } catch (e: Exception) {
                0
            }

            // 只有有待复习内容时才发送通知
            if (dueCount > 0) {
                sendNotification(dueCount)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(dueCount: Int) {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建通知渠道 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "每日中药复习提醒"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 创建点击通知打开应用的 Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_review", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 构建通知
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // 需要创建图标
            .setContentTitle("今日复习时间到！")
            .setContentText("有 $dueCount 味中药待复习，保持记忆不断线")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

/**
 * 提醒调度器
 */
object ReminderScheduler {

    /**
     * 设置每日复习提醒
     * @param hour 提醒时间（小时，24小时制）
     * @param minute 提醒时间（分钟）
     */
    fun scheduleDailyReminder(context: Context, hour: Int = 9, minute: Int = 0) {
        // 计算到目标时间的延迟
        val now = Clock.System.now().toEpochMilliseconds()
        val targetTime = getTargetTimeMillis(hour, minute)
        val initialDelay = if (targetTime > now) {
            targetTime - now
        } else {
            // 如果今天时间已过，安排到明天
            targetTime + (24 * 60 * 60 * 1000L) - now
        }

        // 创建每日重复任务（最小间隔15分钟，但实际按天重复）
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReviewReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(DailyReviewReminderWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyReviewReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    /**
     * 取消每日提醒
     */
    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DailyReviewReminderWorker.WORK_NAME)
    }

    /**
     * 检查提醒是否已设置
     */
    fun isReminderScheduled(context: Context): Boolean {
        val workManager = WorkManager.getInstance(context)
        val workInfos = workManager.getWorkInfosForUniqueWork(DailyReviewReminderWorker.WORK_NAME)
        return workInfos.get()?.any { !it.state.isFinished } == true
    }

    /**
     * 获取目标时间的时间戳
     */
    private fun getTargetTimeMillis(hour: Int, minute: Int): Long {
        val now = java.util.Calendar.getInstance()
        now.set(java.util.Calendar.HOUR_OF_DAY, hour)
        now.set(java.util.Calendar.MINUTE, minute)
        now.set(java.util.Calendar.SECOND, 0)
        now.set(java.util.Calendar.MILLISECOND, 0)
        return now.timeInMillis
    }
}