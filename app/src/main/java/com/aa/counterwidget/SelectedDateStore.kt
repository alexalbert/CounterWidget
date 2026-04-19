package com.aa.counterwidget

import androidx.lifecycle.MutableLiveData
import com.aa.counterwidget.ui.Util
import java.util.Date

object SelectedDateStore {
    val selectedDate = MutableLiveData(Util.today())

    fun setDate(date: Date) {
        selectedDate.value = Util.startOfDay(date)
    }

    fun currentDate(): Date {
        return selectedDate.value ?: Util.today()
    }

    fun move(days: Int) {
        val nextDate = Util.addDays(currentDate(), days)
        if (nextDate.time <= Util.today().time) {
            setDate(nextDate)
        }
    }
}
