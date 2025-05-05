package com.example.hifdhpartner.Helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.hifdhpartner.R
import com.example.hifdhpartner.databases.UserDatabase

class NotificationHelper(private val context: Context) {

    fun showNotification(title: String, message: String, notificationId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "goals_channel",
                "Goals Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for your daily, weekly, and monthly goals"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "goals_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}

class DailyNotificationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Fetch user goals from the database
        val userDao = UserDatabase.getInstance(applicationContext).userDao()
        val userData = userDao.getUserData()

        userData?.let {
            val message = """
                Did you complete your goals today?
                ${it.dailyMemorizationGoal}
                ${it.dailyRevisionGoal}
            """.trimIndent()

            NotificationHelper(applicationContext).showNotification(
                "Daily Goals Reminder",
                message,
                notificationId = 1
            )
        }
        return Result.success()
    }
}

class WeeklyNotificationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userDao = UserDatabase.getInstance(applicationContext).userDao()
        val userData = userDao.getUserData()

        userData?.let {
            val message = """
                Did you complete your goals this week?
                ${it.weeklyMemorizationGoal}
                ${it.weeklyRevisionGoal}
            """.trimIndent()

            NotificationHelper(applicationContext).showNotification(
                "Weekly Goals Reminder",
                message,
                notificationId = 2
            )
        }
        return Result.success()
    }
}

class MonthlyNotificationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userDao = UserDatabase.getInstance(applicationContext).userDao()
        val userData = userDao.getUserData()

        userData?.let {
            val message = """
                Did you complete your goals this month?
                ${it.monthlyMemorizationGoal}
                ${it.monthlyRevisionGoal}
            """.trimIndent()

            NotificationHelper(applicationContext).showNotification(
                "Monthly Goals Reminder",
                message,
                notificationId = 3
            )
        }
        return Result.success()
    }
}


