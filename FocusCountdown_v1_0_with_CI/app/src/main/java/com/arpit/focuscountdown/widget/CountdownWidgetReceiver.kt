package com.arpit.focuscountdown.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import com.arpit.focuscountdown.data.AppStore
import java.util.concurrent.TimeUnit

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}

class CountdownWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = AppStore(context)
        val goal = store.goals().firstOrNull { it.id == store.mainGoalId() && !it.archived }
            ?: store.goals().firstOrNull { !it.archived }
        val remaining = (goal?.targetMillis ?: System.currentTimeMillis()) - System.currentTimeMillis()
        val r = remaining.coerceAtLeast(0)
        val d = TimeUnit.MILLISECONDS.toDays(r)
        val h = TimeUnit.MILLISECONDS.toHours(r) % 24
        val m = TimeUnit.MILLISECONDS.toMinutes(r) % 60
        provideContent {
            Column(
                GlanceModifier.fillMaxSize().padding(16.dp),
                verticalAlignment=Alignment.Vertical.CenterVertically,
                horizontalAlignment=Alignment.Horizontal.CenterHorizontally
            ) {
                Text(goal?.title ?: "FOCUS COUNTDOWN",TextStyle(fontSize=13.sp,color=ColorProvider(0xFF315C4C)))
                Spacer(GlanceModifier.height(6.dp))
                Text("$d DAYS",TextStyle(fontSize=23.sp))
                Text(String.format("%02d : %02d",h,m),TextStyle(fontSize=15.sp))
                Text("Keep going ✦",TextStyle(fontSize=10.sp,color=ColorProvider(0xFFC47A52)))
            }
        }
    }
}
