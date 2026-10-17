package com.ganapujaday.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receiver для обновления виджета:
 * - из AlarmManager (ACTION_UPDATE)
 * - при смене даты в системе (ACTION_DATE_CHANGED)
 * - при смене часового пояса (ACTION_TIMEZONE_CHANGED)
 * - при смене времени (ACTION_TIME_CHANGED)
 *
 * После каждого обновления перепланирует следующий alarm на полночь.
 */
class WidgetUpdateReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_UPDATE = "com.ganapujaday.widget.ACTION_UPDATE"
        private const val TAG = "WidgetUpdateReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Received: ${intent.action}")
        WidgetScheduler.updateNow(context)
        WidgetScheduler.scheduleNext(context)
    }
}