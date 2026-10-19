package com.ganapujaday.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object WidgetScheduler {

    private const val TAG = "WidgetScheduler"
    private const val REQUEST_CODE = 1001

    /**
     * Полное планирование: alarm + periodic WorkManager.
     * Вызывать ТОЛЬКО из onEnabled и BootReceiver.
     */
    fun scheduleNext(context: Context) {
        scheduleAlarm(context)
        enqueuePeriodicWork(context)
    }

    /**
     * Перепланировать только alarm на завтра.
     * Вызывать из WidgetUpdateReceiver и WidgetUpdateWorker после обновления.
     */
    fun rescheduleAlarm(context: Context) {
        scheduleAlarm(context)
    }

    fun cancel(context: Context) {
        cancelAlarm(context)
        cancelPeriodicWork(context)
    }

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

    private fun scheduleAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextMidnight(),
                buildPendingIntent(context),
            )
            Log.i(TAG, "Scheduled alarm at midnight")
        } catch (e: SecurityException) {
            Log.w(TAG, "AlarmManager rejected", e)
        }
    }

    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(buildPendingIntent(context))
        Log.i(TAG, "Cancelled alarm")
    }

    private fun enqueuePeriodicWork(context: Context) {
        val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(computeInitialDelayMinutes(), TimeUnit.MINUTES)
            .addTag(WidgetUpdateWorker.UNIQUE_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WidgetUpdateWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
        Log.i(TAG, "Enqueued periodic worker")
    }

    private fun cancelPeriodicWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WidgetUpdateWorker.UNIQUE_NAME)
        Log.i(TAG, "Cancelled periodic worker")
    }

    private fun computeInitialDelayMinutes(): Long {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atTime(LocalTime.of(0, 15))
        return java.time.Duration.between(now, nextMidnight).toMinutes()
    }

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