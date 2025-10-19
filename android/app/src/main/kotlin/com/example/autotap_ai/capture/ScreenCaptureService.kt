package com.example.autotap_ai.capture

import android.app.*
import android.content.*
import android.graphics.*
import android.hardware.display.MediaProjectionManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.autotap_ai.R
import com.example.autotap_ai.access.TapAccessibilityService
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ScreenCaptureService : Service() {
    companion object {
        const val ACTION_START = "com.example.autotap_ai.action.START"
        const val ACTION_STOP = "com.example.autotap_ai.action.STOP"
        const val EXTRA_TEMPLATE_PATH = "template_path"
        private const val CHANNEL = "autotap_capture"
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

        // Richiede consenso di cattura schermo (scaffold)
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = mpm.createScreenCaptureIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(captureIntent)

        // Loop semplificato (placeholder): qui dovresti acquisire un frame e chiamare processFrame(screen)
        handler.post(loop)
    }

    private val loop = object : Runnable {
        override fun run() {
            if (!running) return

            // TODO: sostituire con frame reale catturato da MediaProjection.
            // Placeholder: bitmap vuoto per dimostrazione compilazione.
            val bmp = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            processFrame(bmp)

            handler.postDelayed(this, 300) // 300ms come richiesto
        }
    }

    private fun processFrame(screen: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(screen, 0)
        recognizer.process(image).addOnSuccessListener { result ->
            result.textBlocks.forEach { block ->
                val box = block.boundingBox
                if (block.text.matches(Regex("(?i)(ok|play|avvia|gioca|chiudi)"))) {
                    box?.center()?.let { c -> sendTap(c.x, c.y) }
                }
            }
        }
        // TODO: aggiungere vero template matching su 'templatePath' e confrontarlo con 'screen'.
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
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "AutoTap", NotificationManager.IMPORTANCE_LOW))
        }
        val notif = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("AutoTap attivo")
            .setContentText("Rilevamento in corso (no autostart)")
            .build()
        startForeground(NOTIF_ID, notif)
    }

    override fun onDestroy() {
        running = false
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
