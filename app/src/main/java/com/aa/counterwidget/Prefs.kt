package com.aa.counterwidget

import android.content.Context

private const val PREFS_NAME = "com.aa.counterWidget.CounterWidget"
internal const val COUNT = "count_"
internal const val COLOR = "color_"

internal fun savePref(context: Context, appWidgetId: Int, pref: String, count: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putInt(pref + appWidgetId, count)
    prefs.apply()
}

internal fun loadPref(context: Context, appWidgetId: Int, pref: String): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    return prefs.getInt(pref + appWidgetId, 0)
}

internal fun deletePref(context: Context, appWidgetId: Int, pref: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(pref + appWidgetId)
    prefs.apply()
}
