package com.ganapujaday.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Планировщик обновления виджета раз в сутки в полночь.
 *
 * Почему AlarmManager, а не только WorkManager:
 * - WorkManager PeriodicWorkRequest имеет минимальный интервал 15 минут
 *   и не гарантирует точное время. Для календаря нужна полночь.
 * - setAndAllowWhileIdle срабатывает даже в Doze (с задержкой до ~9 минут).
 * - Квоты exact alarm на Android 14+ не используем (SET_AND_ALLOW_WHILE_IDLE
 *   не требует разрешения).
 *
 * Почему WorkManager как fallback:
 * - AlarmManager может быть убит системой при агрессивной экономии батареи
 *   (некоторые MIUI/EMUI/Oppo).
 * - WorkManager с 24-часовым интервалом страхует: даже если alarm потерян,
 *   следующее обновление придёт в течение суток.
 */
object WidgetScheduler {

    private const val TAG = "WidgetScheduler"
    private const val REQUEST_CODE = 1001

    /**
     * Планирует следующее обновление виджета на ближайшую полночь + 30 секунд.
     * Вызывается при создании виджета (onEnabled) и после каждого обновления.
     */
    fun scheduleNext(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val triggerAt = nextMidnight()
        val pendingIntent = buildPendingIntent(context)

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent,
            )
            Log.i(TAG, "Scheduled widget update at ${LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(triggerAt),
                ZoneId.systemDefault()
            )}")
        } catch (e: SecurityException) {
            // На Android 12+ может отсутствовать SCHEDULE_EXACT_ALARM.
            // setAndAllowWhileIdle не требует разрешения, но если попадём сюда —
            // логируем и полагаемся на WorkManager fallback.
            Log.w(TAG, "AlarmManager rejected, relying on WorkManager fallback", e)
        }
    }

    /**
     * Отменяет запланированный alarm. Вызывается при удалении последнего виджета (onDisabled).
     */
    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context))
        Log.i(TAG, "Cancelled widget update alarm")
    }

    /**
     * Немедленно обновляет все экземпляры виджета.
     * Вызывается из BroadcastReceiver при получении ACTION_UPDATE.
     */
    fun updateNow(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                GanapujaDayWidget().updateAll(context)
                Log.i(TAG, "Widget updated on demand")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update widget", e)
            }
        }
    }

    /**
     * Следующая полночь + 30 секунд в текущем часовом поясе.
     * +30 секунд — чтобы гарантированно попасть в новый день,
     * даже если часы системы сдвинуты на несколько секунд.
     */
    private fun nextMidnight(): Long {
        val zone = ZoneId.systemDefault()
        val tomorrow = LocalDate.now(zone).plusDays(1)
        val midnight = LocalDateTime.of(tomorrow, LocalTime.of(0, 0, 30))
        return midnight.atZone(zone).toInstant().toEpochMilli()
    }

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WidgetUpdateReceiver::class.java).apply {
            action = WidgetUpdateReceiver.ACTION_UPDATE
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}