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
import java.util.*
import kotlin.collections.HashMap


const val CLICK_ACTION = "android.appwidget.action.CLICK"
const val WIDGET = "mywidget"

/**
 * Implementation of App Widget functionality.
 */
class CounterWidget : AppWidgetProvider() {

    companion object {
        private var lastClickTs = 0L
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(WIDGET, "onupdate")
        // There may be multiple widgets active, so update all of them
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.counter_widget)
            val count = loadPref(context, widgetId, COUNT)
            val bkColor = loadPref(context, widgetId, COLOR)
            updateAppWidget(context, appWidgetManager, widgetId, count, bkColor)
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds?.forEach { id ->
            deletePref(context!!, id, COUNT)
            deletePref(context!!, id, COLOR)
        }
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

        Log.d(WIDGET, "onreceive")

        var count: Int

        if (intent!!.action == CLICK_ACTION) {
            val widgetId = intent!!.data!!.lastPathSegment!!.toInt()
            if (isDoubleClick()) {
                count = 0
            } else {
                count = loadPref(context, widgetId!!, COUNT)
                count++
            }
            savePref(context, widgetId!!, COUNT, count)

            var t = Toast(context)
            t.setText(intent.data.toString())
            t.show()


            views.setTextViewText(R.id.counter_button, count.toString())
            appWidgetManager.updateAppWidget(intArrayOf(widgetId!!.toInt()), views)
        }
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
    Log.d(WIDGET, "in updateAppWidget")

    // Construct and set click intent
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


