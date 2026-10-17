package com.ganapujaday.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Перепланирует обновление виджета после перезагрузки.
 *
 * Почему нужен:
 * - AlarmManager теряет PendingIntent при reboot.
 * - Без этого виджет покажет вчерашний день до первого действия пользователя.
 *
 * Почему проверяем наличие виджета через AppWidgetManager:
 * - BOOT_COMPLETED приходит на все приложения с разрешением, но планировать
 *   alarm имеет смысл только если виджет реально добавлен на домашний экран.
 *   Иначе мы будим систему зря.
 */
class BootReceiver : BroadcastReceiver() {

    private companion object {
        const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.i(TAG, "BOOT_COMPLETED received, checking widget presence")

        if (!isWidgetAdded(context)) {
            Log.i(TAG, "Widget not added, skipping schedule")
            return
        }

        Log.i(TAG, "Widget found, rescheduling midnight update")
        WidgetScheduler.scheduleNext(context)
    }

    /**
     * Проверяет, есть ли хотя бы один экземпляр виджета на домашнем экране.
     */
    private fun isWidgetAdded(context: Context): Boolean {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, GanapujaDayWidgetReceiver::class.java)
        val ids = manager.getAppWidgetIds(component)
        return ids.isNotEmpty()
    }
}