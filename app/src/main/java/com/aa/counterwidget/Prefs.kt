package com.aa.counterwidget

import android.content.Context

private const val PREFS_NAME = "com.aa.counterWidget.CounterWidget"
internal const val COLOR = "color_"

/**
 * COLOR pref maps widget id to color
 */

internal fun savePref(context: Context, appWidgetId: Int, pref: String, value: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putInt(pref + appWidgetId, value)
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

internal fun getWidgetIds(context: Context) : ArrayList<Int> {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val ids = ArrayList<Int>()
    for (id in prefs.all.keys) {
        if (id.startsWith(COLOR)) {
            ids.add(id.removePrefix(COLOR).toInt())
        }
    }
    return ids
}
