package com.ganapujaday.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ganapujaday.MainActivity
import com.ganapujaday.cache.TibetanDayCache
import com.ganapujaday.core.Tyme4KtRepository
import java.time.LocalDate
import androidx.compose.ui.graphics.Color
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.width

class GanapujaDayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val cache = TibetanDayCache(Tyme4KtRepository())
        val today = LocalDate.now()
        val day = try {
            cache.get(today)
        } catch (e: Exception) {
            null
        }

        // Intent создаётся здесь, потому что context доступен только в provideGlance.
        // Внутри provideContent { } context не в scope.
        val intent = Intent(context, MainActivity::class.java)

        provideContent {
            GlanceTheme {
                WidgetContent(
                    intent = intent,
                    lunarDay = day?.day,
                    isSpecial = day?.isSpecial == true,
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    intent: Intent,
    lunarDay: Int?,
    isSpecial: Boolean,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .background(Color.Transparent)
            .clickable(actionStartActivity(intent)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (lunarDay != null) {
            Text(
                text = "$lunarDay ☾",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 24.sp,
                ),
            )
            if (isSpecial) {
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = "Ганапуджа",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 12.sp,
                    ),
                )
            }
        } else {
            Text(
                text = "—",
                style = TextStyle(color = GlanceTheme.colors.onSurface),
            )
        }
    }
}

class GanapujaDayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GanapujaDayWidget()
}