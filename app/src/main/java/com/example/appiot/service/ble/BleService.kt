package com.example.appiot.service.ble

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.appiot.R

class BleService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY // Asegura que el servicio se reinicie si el sistema lo elimina por falta de RAM
    }

    private fun createNotification(): Notification {
        val channelId = "ble_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "BLE Connection Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ESP-32 Conectado")
            .setContentText("Manteniendo comunicación BLE activa")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth) // Asegúrate de tener este icono en res/drawable
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}