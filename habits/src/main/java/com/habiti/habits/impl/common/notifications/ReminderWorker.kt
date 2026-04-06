package com.habiti.habits.impl.common.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.habiti.habits.impl.R

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val channel = "habits_channel"

    override suspend fun doWork(): Result {
        val habitId = inputData.getString("habit_id") ?: return Result.failure()
        val habitName = inputData.getString("habit_name") ?: return Result.failure()

        setForeground(createForegroundInfo(habitName))
        sendNotification(habitId, habitName)
        return Result.success()
    }

    private fun createForegroundInfo(habitName: String): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, channel)
            .setContentTitle("Habiti")
            .setContentText("Напоминание о привычке: $habitName")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        return ForegroundInfo(1, notification)
    }

    private fun sendNotification(habitId: String, habitName: String) {
        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        createNotificationChannel(notificationManager)

        val intent = applicationContext.packageManager.getLaunchIntentForPackage(
            applicationContext.packageName
        )?.apply {
            putExtra("habit_id", habitId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channel)
            .setContentTitle("Habiti")
            .setContentText(applicationContext.getString(R.string.reminder_time_for_habit, habitName))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(habitId.hashCode(), notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                this.channel,
                applicationContext.getString(R.string.reminder_habit),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = applicationContext.getString(R.string.reminder_habit_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}