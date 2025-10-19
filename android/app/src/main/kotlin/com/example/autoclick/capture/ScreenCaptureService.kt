package com.example.autoclick.capture

import android.app.*
import android.content.*
import android.graphics.*
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.autoclick.R
import com.example.autoclick.access.TapAccessibilityService

class ScreenCaptureService : Service() {
    companion object {
        const val ACTION_START = "com.example.autoclick.action.START"
        const val ACTION_STOP = "com.example.autoclick.action.STOP"
        const val EXTRA_TEMPLATE_PATH = "template_path"
        private const val CHANNEL = "autoclick_capture"
        private const val NOTIF_ID = 42
    }

    private var templatePath: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startCapture(intent.getStringExtra(EXTRA_TEMPLATE_PATH))
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startCapture(tPath: String?) {
        templatePath = tPath
        createNotification()
        running = true
        handler.post(loop)
    }

    private val loop = object : Runnable {
        override fun run() {
            if (!running) return
            // TODO: sostituire con frame reale + matching
            val bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            processFrame(bmp)
            handler.postDelayed(this, 300)
        }
    }

    private fun processFrame(screen: Bitmap) {
        // TODO: OCR/Template matching → sendTap(x, y)
    }

    private fun sendTap(x: Int, y: Int) {
        val it = Intent(TapAccessibilityService.ACTION_TAP_AT).apply {
            putExtra(TapAccessibilityService.EXTRA_X, x)
            putExtra(TapAccessibilityService.EXTRA_Y, y)
        }
        sendBroadcast(it)
    }

    private fun createNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL) == null) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "AutoClick", NotificationManager.IMPORTANCE_LOW))
        }
        val notif = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("AutoClick attivo")
            .setContentText("Servizio in esecuzione (V3 Full Clean)")
            .build()
        startForeground(NOTIF_ID, notif)
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
