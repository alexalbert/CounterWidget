package com.aa.counterwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import java.net.URI
import java.util.*
import kotlin.collections.HashMap


var REFRESH_ACTION = "android.appwidget.action.APPWIDGET_UPDATE"
const val CLICK_ACTION = "android.appwidget.action.CLICK"

/**
 * Implementation of App Widget functionality.
 */
class CounterWidget : AppWidgetProvider() {

    companion object {
        private var counts = HashMap<String, Int>()
        private var lastClickTs = 0L
    }

    private var views: RemoteViews? = null



    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, 0, -1)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val views = RemoteViews(context!!.packageName, R.layout.counter_widget)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(
                context,
                CounterWidget::class.java
            )
        )

        var count: Int = 0

        if (intent!!.action == CLICK_ACTION) {
            val widgetId = intent!!.data!!.lastPathSegment
            if (counts.containsKey(widgetId)) {
                if(!isDoubleClick()) { // Doubleclick zeroes counter
                    count = counts[widgetId]!!
                    if (count == 0) {
                        val cached = Persistence.read(context, widgetId!!)
                        count = cached.count
                    }
                    if (count != null) {
                        count++
                    }
                }
            }

            counts[widgetId!!] = count

            Persistence.write(context, widgetId, Snapshot(day(), count))

            var t = Toast(context)
            t.setText(intent.data.toString())
            t.show()
            views.setTextViewText(R.id.counter_button, count.toString())
            appWidgetManager.updateAppWidget(intArrayOf(widgetId!!.toInt()), views)
        } else { // Resize
            count = 0
            views.setTextViewText(R.id.counter_button, count.toString())
            appWidgetManager.updateAppWidget(appWidgetIds, views)
        }
    }

    private fun day(): Int {
        var c = Calendar.getInstance()
        return c.get(Calendar.DAY_OF_YEAR)
    }

    private fun isDoubleClick(): Boolean {
        val ts = Date().time
        var ret = false
        if (lastClickTs != 0L) {
            ret = ts - lastClickTs  < 200
        }
        lastClickTs = ts
        return ret
    }

}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    count: Int,
    background: Int
) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.counter_widget)
    views.setTextViewText(R.id.counter_button, count.toString())
    if (background != -1) {
        views.setInt(R.id.frame, "setBackgroundColor", background)
    }

    val intent = Intent(context, CounterWidget::class.java)
    val data: Uri = Uri.withAppendedPath(
        Uri.parse("URI_SCHEME" + "://widget/id/"), appWidgetId.toString())

    intent.data = data
    intent.action = CLICK_ACTION
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

    views.setOnClickPendingIntent(R.id.counter_button, pendingIntent)


    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}