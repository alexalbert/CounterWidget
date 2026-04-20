package com.aa.counterwidget.ui

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.widget.TextView
import com.aa.counterwidget.R
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

        fun updateDateHeader(dateView: TextView, date: Date, defaultTextColor: Int) {
            if (isSameDay(date, Date())) {
                dateView.text = "Today"
                dateView.setTextColor(dateView.context.getColor(R.color.light_blue_50))
            } else {
                dateView.text = formatDate(date)
                dateView.setTextColor(defaultTextColor)
            }
        }

        fun startOfDay(date: Date): Date {
            val cal = Calendar.getInstance()
            cal.time = date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }

        fun today(): Date {
            return startOfDay(Date())
        }

        fun addDays(date: Date, days: Int): Date {
            val cal = Calendar.getInstance()
            cal.time = startOfDay(date)
            cal.add(Calendar.DAY_OF_YEAR, days)
            return cal.time
        }

        fun isSameDay(first: Date, second: Date): Boolean {
            return startOfDay(first).time == startOfDay(second).time
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
