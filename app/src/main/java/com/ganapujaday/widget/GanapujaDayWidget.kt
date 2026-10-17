package com.ganapujaday.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.ColorFilter
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.ganapujaday.MainActivity
import com.ganapujaday.cache.TibetanDayCache
import com.ganapujaday.core.Tyme4KtRepository
import java.time.LocalDate

class GanapujaDayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val cache = TibetanDayCache(Tyme4KtRepository())
        val today = LocalDate.now()
        val day = try {
            cache.get(today)
        } catch (e: Exception) {
            null
        }

        val intent = Intent(context, MainActivity::class.java)

        provideContent {
            GlanceTheme {
                WidgetContent(
                    context = context,
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
    context: Context,
    intent: Intent,
    lunarDay: Int?,
    isSpecial: Boolean,
) {
    val foreground = GlanceTheme.colors.onSurface

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
                text = lunarDay.toString(),
                style = TextStyle(
                    color = foreground,
                    fontSize = 24.sp,
                ),
            )

            val icon = moonIconFor(lunarDay)
            if (icon != null) {
                Spacer(GlanceModifier.width(6.dp))
                Image(
                    provider = ImageProvider(icon),
                    contentDescription = null,
                    modifier = GlanceModifier.size(24.dp),
                    colorFilter = ColorFilter.tint(
                        ColorProvider(foreground.getColor(context)),
                    ),
                )
            }

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
                style = TextStyle(color = foreground),
            )
        }
    }
}