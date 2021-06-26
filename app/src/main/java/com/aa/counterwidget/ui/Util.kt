package com.aa.counterwidget.ui

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import java.text.SimpleDateFormat
import java.util.*

class Util {
    companion object {
        fun getDefaultBackground(context: Context) : Int {
            var color =  Color.WHITE
            val a = TypedValue()
            context.theme.resolveAttribute(android.R.attr.windowBackground, a, true)
            if (a.isColorType) {
                color = a.data
            }
            return color
        }

        private var timeFormat = SimpleDateFormat("kk:mm")

        fun formatTime(date: Date): String {
            return timeFormat.format(date)
        }
    }
}