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
        private var dateFormat = SimpleDateFormat("yyyy-MM-dd")

        fun formatTime(date: Date): String {
            return timeFormat.format(date)
        }

        fun formatDate(date: Date): String {
            return dateFormat.format(date)
        }


        private var lastClickTs = 0L
        fun isDoubleClick(): Boolean {
            val ts = Date().time
            var ret = false
            if (lastClickTs != 0L) {
                ret = ts - lastClickTs  < 300
            }
            lastClickTs = ts
            return ret
        }
    }
}
