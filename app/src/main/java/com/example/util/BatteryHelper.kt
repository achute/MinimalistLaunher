package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class BatteryStatus(
    val level: Int = 100,
    val isCharging: Boolean = false,
    val chargingSource: String = "Discharging",
    val temperatureCelsius: Float = 25f,
    val health: String = "Good",
    val voltageMv: Int = 4000
)

object BatteryHelper {

    fun getBatteryStatusFlow(context: Context): Flow<BatteryStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent == null) return
                val status = parseBatteryIntent(intent)
                trySend(status)
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        try {
            val initialIntent = ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            if (initialIntent != null) {
                trySend(parseBatteryIntent(initialIntent))
            }
        } catch (e: Exception) {
            // Fallback for environments where ContextCompat or export flags differ
            try {
                @Suppress("DEPRECATION")
                val initialIntent = context.registerReceiver(receiver, filter)
                if (initialIntent != null) {
                    trySend(parseBatteryIntent(initialIntent))
                }
            } catch (ex: Exception) {
                // Return default status
                trySend(BatteryStatus())
            }
        }

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Ignore if unregistered
            }
        }
    }

    private fun parseBatteryIntent(intent: Intent): BatteryStatus {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else 100

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val chargingSource = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Charging"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Charging"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Charging"
            else -> if (isCharging) "Charging" else "Discharging"
        }

        val tempRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
        val tempCelsius = tempRaw / 10f

        val healthRaw = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        val health = when (healthRaw) {
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Good"
        }

        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000)

        return BatteryStatus(
            level = batteryPct,
            isCharging = isCharging,
            chargingSource = chargingSource,
            temperatureCelsius = tempCelsius,
            health = health,
            voltageMv = voltage
        )
    }
}
