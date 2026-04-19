package com.aa.counterwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aa.counterwidget.ui.Util
import java.util.Calendar
import java.util.Date

private const val ROLLOVER_PREFS = "daily_rollover"
private const val LAST_ROLLOVER_DATE = "last_rollover_date"
private const val MIDNIGHT_ROLLOVER_ACTION = "com.aa.counterwidget.action.MIDNIGHT_ROLLOVER"

class DailyRolloverReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val safeContext = context ?: return
        val action = intent?.action ?: return

        if (
            action == Intent.ACTION_DATE_CHANGED ||
            action == MIDNIGHT_ROLLOVER_ACTION ||
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            handleAction(safeContext, action)
        }
    }

    private fun handleAction(context: Context, action: String) {
        if (action == Intent.ACTION_DATE_CHANGED || action == MIDNIGHT_ROLLOVER_ACTION) {
            rolloverPreviousDateIfNeeded(context)
        }
        scheduleNextMidnight(context)
    }

    private fun rolloverPreviousDateIfNeeded(context: Context) {
        val previousDate = previousDateAtMidnight()
        val previousDateMillis = previousDate.time

        val prefs = context.getSharedPreferences(ROLLOVER_PREFS, 0)
        val lastRollover = prefs.getLong(LAST_ROLLOVER_DATE, -1L)
        if (lastRollover == previousDateMillis) {
            Log.i(TAG, "Daily rollover skipped for ${Util.formatDate(previousDate)} (already done)")
            return
        }

        val snapshots = arrayListOf<PeriodColorCount>()
        val widgetIds = getWidgetIds(context)
        val detailedData = TsDataUtil.snapshot(context)

        for (widgetId in widgetIds) {
            val color = loadPref(context, widgetId, COLOR)
            if (color == 0) continue

            val count = TsDataUtil.getWidgetCount(context, widgetId)
            snapshots.add(PeriodColorCount(widgetId, color, count))
            TsDataUtil.clearWidgetData(context, widgetId)
        }

        if (snapshots.isNotEmpty()) {
            HistoryDataUtil.addDailySnapshot(context, previousDate, snapshots, detailedData)
        }

        prefs.edit().putLong(LAST_ROLLOVER_DATE, previousDateMillis).apply()
        CounterWidget.updateWidgets(context)
        Log.i(TAG, "Daily rollover completed for ${Util.formatDate(previousDate)}")
    }

    private fun scheduleNextMidnight(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = nextMidnightMillis()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            Intent(context, DailyRolloverReceiver::class.java).apply {
                action = MIDNIGHT_ROLLOVER_ACTION
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        Log.i(
            TAG,
            "Next midnight rollover scheduled for ${Util.formatDate(Date(triggerAtMillis))} " +
                "(${Date(triggerAtMillis)})"
        )
    }

    private fun previousDateAtMidnight(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun nextMidnightMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    companion object {
        fun ensureRolloverSchedule(context: Context) {
            DailyRolloverReceiver().scheduleNextMidnight(context)
        }
    }
}
