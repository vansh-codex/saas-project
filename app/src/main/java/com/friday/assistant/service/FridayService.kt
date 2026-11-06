package com.friday.assistant.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.friday.assistant.MainActivity
import com.friday.assistant.R

/**
 * Foreground service that keeps an assistant session alive while Friday is listening or
 * speaking, so the OS does not kill the microphone session mid-utterance.
 */
class FridayService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startSession()
            ACTION_STOP -> stopSession()
            else -> stopSession()
        }
        return START_NOT_STICKY
    }

    private fun startSession() {
        createNotificationChannel()
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), serviceType)
    }

    private fun stopSession() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, FridayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mic)
            .setContentTitle(getString(R.string.notification_title))
            .setContentIntent(contentIntent)
            .addAction(0, getString(R.string.notification_action_stop), stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START: String = "com.friday.assistant.action.START_SESSION"
        const val ACTION_STOP: String = "com.friday.assistant.action.STOP_SESSION"

        private const val CHANNEL_ID = "friday_session"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            context.startForegroundService(Intent(context, FridayService::class.java).setAction(ACTION_START))
        }

        fun stop(context: Context) {
            context.startService(Intent(context, FridayService::class.java).setAction(ACTION_STOP))
        }
    }
}
