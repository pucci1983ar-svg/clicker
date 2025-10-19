package com.example.autoclick

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.example.autoclick.capture.ScreenCaptureService

class MainActivity: FlutterActivity() {
    private val channel = "autoclick/native"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channel).setMethodCallHandler { call, result ->
            when (call.method) {
                "start" -> {
                    val templatePath = call.argument<String>("templatePath")
                    val it = Intent(this, ScreenCaptureService::class.java).apply {
                        action = ScreenCaptureService.ACTION_START
                        putExtra(ScreenCaptureService.EXTRA_TEMPLATE_PATH, templatePath)
                    }
                    startService(it)
                    result.success(true)
                }
                "stop" -> {
                    val it = Intent(this, ScreenCaptureService::class.java).apply {
                        action = ScreenCaptureService.ACTION_STOP
                    }
                    startService(it)
                    result.success(true)
                }
                "openAccessibility" -> {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    result.success(true)
                }
                "openOverlayPermission" -> {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivity(intent)
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }
    }
}
