package com.nextgenbuildpro.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/** Restarts background services after device reboot. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val hermesIntent = Intent(context, HermesBackgroundService::class.java)
            val brainIntent = Intent(context, UnifiedBrainBackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(hermesIntent)
                context.startForegroundService(brainIntent)
            } else {
                context.startService(hermesIntent)
                context.startService(brainIntent)
            }
        }
    }
}
