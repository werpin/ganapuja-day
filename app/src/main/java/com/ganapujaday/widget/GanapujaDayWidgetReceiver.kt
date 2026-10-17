package com.ganapujaday.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class GanapujaDayWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = GanapujaDayWidget()

    /**
     * Вызывается, когда добавлен первый экземпляр виджета.
     * Планируем полночное обновление.
     */
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetScheduler.scheduleNext(context)
    }

    /**
     * Вызывается, когда удалён последний экземпляр виджета.
     * Отменяем alarm, чтобы не будить систему зря.
     */
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetScheduler.cancel(context)
    }
}