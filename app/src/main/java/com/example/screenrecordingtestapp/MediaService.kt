package com.example.screenrecordingtestapp


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder

import androidx.core.app.NotificationCompat



class MediaService : Service() {
    private val NOTIFICATION_CHANNEL_ID = "com.example.screenrecordingtestapp.MediaService"
    private val NOTIFICATION_CHANNEL_NAME = "example.screenrecordingtestapp.channel_screen_record"
    private val NOTIFICATION_CHANNEL_DESC = "example.screenrecordingtestapp.channel_screen_record"


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        return Binder()
    }

    private fun createNotificationChannel() {
        val notificationIntent = Intent(this, MediaService::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        resources,
                        R.drawable.ic_launcher
                    )
                )
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Starting Service")
                .setContentText("Starting monitoring service")
                .setContentIntent(pendingIntent)
        val notification = notificationBuilder.build()
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = NOTIFICATION_CHANNEL_DESC
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager!!.createNotificationChannel(channel)
//        startForeground(
//            1,
//            notification
//        )
        startForeground(
            1,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
    }
}