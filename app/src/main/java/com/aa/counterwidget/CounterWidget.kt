package com.aa.counterwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import com.aa.counterwidget.ui.Util

const val CLICK_ACTION = "android.appwidget.action.CLICK"

const val TAG = "====================="



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

            Log.i(TAG, "Updating widget $widgetId")
            val count = TsDataUtil.getWidgetCount(context, widgetId)
            Log.i(TAG, "Count $count")
            val bkColor = loadPref(context, widgetId, COLOR)
            Log.i(TAG, "Color $bkColor")
            updateAppWidget(context, appWidgetManager, widgetId, count, bkColor)
            Log.i(TAG, "Update complete widget $widgetId")
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
            Log.i(TAG, "onReceive action=${intent.action} data=${intent.data}")
            val widgetId = intent.data!!.lastPathSegment!!.toInt()
            if (Util.isDoubleClick()) {
                val currentCount = TsDataUtil.getWidgetCount(context, widgetId)
                val color = loadPref(context, widgetId, COLOR)
                HistoryDataUtil.addPeriod(context, widgetId, color, currentCount)
                count = 0
                TsDataUtil.clearWidgetData(context, widgetId)
            } else {
                count = TsDataUtil.getWidgetCount(context, widgetId)
                count++
                TsDataUtil.addTs(context, widgetId)
            }

            views.setTextViewText(R.id.counter_button, count.toString())
            val bkColor = loadPref(context, widgetId, COLOR)
            if (bkColor != -1) {
                views.setInt(R.id.frame, "setBackgroundColor", bkColor)
                Log.i(TAG, "Click update widget $widgetId count=$count color=$bkColor")
            } else {
                Log.i(TAG, "Click update widget $widgetId count=$count color=unset")
            }
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
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        appWidgetId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    Log.i(TAG, "PendingIntent widget $appWidgetId action=$CLICK_ACTION data=${intent.data}")
    views.setOnClickPendingIntent(R.id.counter_button, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
    Log.i(TAG, "Updating widget $appWidgetId")
}


