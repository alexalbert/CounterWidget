package com.aa.counterwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.aa.counterwidget.ui.Util

const val CLICK_ACTION = "android.appwidget.action.CLICK"

/**
 * Implementation of App Widget functionality.
 */
class CounterWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (widgetId in appWidgetIds) {
            val count = TsDataUtil.getWidgetCount(context, widgetId)
            val bkColor = loadPref(context, widgetId, COLOR)

            updateAppWidget(context, appWidgetManager, widgetId, count, bkColor)
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds?.forEach { id ->
            deletePref(context!!, id, COLOR)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        val views = RemoteViews(context!!.packageName, R.layout.counter_widget)
        val appWidgetManager = AppWidgetManager.getInstance(context)

        var count: Int

        if (intent!!.action == CLICK_ACTION) {
            val widgetId = intent.data!!.lastPathSegment!!.toInt()
            if (Util.isDoubleClick()) {
                count = 0
                TsDataUtil.clearWidgetData(context, widgetId)
            } else {
                count = TsDataUtil.getWidgetCount(context, widgetId)
                count++
                TsDataUtil.addTs(context, widgetId)
            }

            views.setTextViewText(R.id.counter_button, count.toString())
            appWidgetManager.updateAppWidget(intArrayOf(widgetId), views)
        }
    }


    companion object {
        fun updateWidgets(context: Context) {
            val intent = Intent(context, CounterWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = getWidgetIds(context).toIntArray()
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
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

    // Construct and set click intent
    val intent = Intent(context, CounterWidget::class.java)
    val data: Uri = Uri.withAppendedPath(
        Uri.parse("URI_SCHEME" + "://widget/id/"), appWidgetId.toString())
    intent.data = data
    intent.action = CLICK_ACTION
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    views.setOnClickPendingIntent(R.id.counter_button, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}


