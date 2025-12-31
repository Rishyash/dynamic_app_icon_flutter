package com.application.dynamic_app_icon_flutter_plus

import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

class DynamicAppIconFlutterPlusPlugin : FlutterPlugin {
  private var channel: MethodChannel? = null
  
  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    setupChannel(binding.binaryMessenger, binding.applicationContext)
  }
  
  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    teardownChannel()
  }
  
  private fun setupChannel(messenger: BinaryMessenger, context: Context) {
    channel = MethodChannel(messenger, CHANNEL_NAME)
    val handler = MethodCallHandlerImpl(context)
    channel?.setMethodCallHandler(handler)
  }
  
  private fun teardownChannel() {
    channel?.setMethodCallHandler(null)
    channel = null
  }
  
  companion object {
    private const val CHANNEL_NAME = "flutter_dynamic_icon"
  }
}


