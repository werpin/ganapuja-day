package com.ganapujaday.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log
import androidx.glance.appwidget.updateAll

/**
 * Worker для гарантированного обновления виджета.
 *
 * Почему WorkManager:
 * - На MIUI/EMUI/Oppo AlarmManager блокируется агрессивной экономией батареи.
 * - WorkManager использует JobScheduler, который эти прошивки уважают больше.
 * - PeriodicWorkRequest(24h) гарантирует обновление в течение суток,
 *   даже если точный alarm потерян.
 *
 * Почему updateAll, а не broadcast:
 * - Worker работает в фоне, система может не доставить broadcast виджетам
 *   так же надёжно, как прямой вызов Glance.
 * - updateAll() перерисовывает все экземпляры виджета атомарно.
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "Worker started, attempt=$runAttemptCount")
        return try {
            GanapujaDayWidget().updateAll(applicationContext)
            WidgetScheduler.rescheduleAlarm(applicationContext)  // было: scheduleNext
            Log.i(TAG, "Widget updated by Worker")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed", e)
            Result.success()  // не останавливаем periodic work, следующий запуск через 24h
        }
    }

    companion object {
        const val TAG = "WidgetUpdateWorker"
        const val UNIQUE_NAME = "ganapuja_widget_daily"
    }
}