package com.japygo.modakmodak.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.japygo.modakmodak.MainActivity
import com.japygo.modakmodak.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "modak_timer_channel"
        const val STUDY_NOTIFICATION_ID = 101
        const val BREAK_NOTIFICATION_ID = 102
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notif_channel_name)
            val descriptionText = context.getString(R.string.notif_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showStudyFinishedNotification(languageCode: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Ensure we use the correct locale based on app language setting
        val locale = java.util.Locale(languageCode)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        // Pick random title and text
        val titles = localizedContext.resources.getStringArray(R.array.notif_study_finished_titles)
        val texts = localizedContext.resources.getStringArray(R.array.notif_study_finished_texts)
        val randomIndex = (titles.indices).random()

        val title = titles.getOrElse(randomIndex) { titles[0] }
        val text = texts.getOrElse(randomIndex) { texts[0] }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(STUDY_NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun showBreakFinishedNotification(languageCode: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Ensure we use the correct locale based on app language setting
        val locale = java.util.Locale(languageCode)
        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)

        // Pick random title and text
        val titles = localizedContext.resources.getStringArray(R.array.notif_break_finished_titles)
        val texts = localizedContext.resources.getStringArray(R.array.notif_break_finished_texts)
        val randomIndex = (titles.indices).random()

        val title = titles.getOrElse(randomIndex) { titles[0] }
        val text = texts.getOrElse(randomIndex) { texts[0] }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Placeholder
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(BREAK_NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
    fun showDailyReminder(languageCode: String) {
        val title = if (languageCode == "ko") "오늘 학습을 잊지 않으셨나요? 🔥" else "Don't forget to study today! 🔥"
        val text = if (languageCode == "ko") "모닥불이 기다리고 있어요. 잠깐이라도 들러주세요." else "Your Modak is waiting. Come by for a bit."
        
        showNotification(title, text)
    }

    fun showComebackNotification(languageCode: String, days: Int) {
        val title = if (languageCode == "ko") "모닥불이 꺼져가고 있어요... 😢" else "The fire is fading... 😢"
        val text = if (languageCode == "ko") 
            "${days}일 동안 뵙지 못했네요. 다시 돌아오셔서 온기를 나눠주세요." 
        else 
            "We haven't seen you for $days days. Come back and share the warmth."

        showNotification(title, text)
    }

    private fun showNotification(title: String, text: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}
