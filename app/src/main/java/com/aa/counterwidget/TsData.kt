package com.aa.counterwidget

import android.content.Context
import com.aa.counterwidget.ui.Util
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import java.io.*
import java.util.*

class TsData: HashMap<Int, ArrayList<Date>>(), Serializable
data class TsColorItem(val date: Date, val colors: ArrayList<Int>)

data class PeriodColorCount(val widgetId: Int, val color: Int, var count: Int) : Serializable
data class PeriodSummary(val date: Date, val counts: ArrayList<PeriodColorCount>) : Serializable
class PeriodHistory : ArrayList<PeriodSummary>(), Serializable

class TsDataUtil {

    companion object {
        private const val FILE_NAME = "timestamps"

        fun addTs(context: Context, widgetId: Int)  {
            val map = read(context)
            if (map.containsKey(widgetId))  {
                map[widgetId]?.add(Date())
            } else {
                map[widgetId] = arrayListOf(Date())
            }
            write(context, map)
        }

        fun removeTs(context: Context, date: String)  {
            val map = read(context)
            for (widget in map.values) {
                var forRemoval: Int? = null

                for((index, time)  in widget.withIndex()) {
                    if (date == Util.formatTime(time))
                        forRemoval = index
                }
                if (forRemoval != null) {
                    widget.removeAt(forRemoval)
                    break
                }
            }
            write(context, map)
        }

        private fun write(context: Context, map: TsData) {
            val file = File(context.applicationInfo.dataDir, FILE_NAME)
            val outputStream = ObjectOutputStream(FileOutputStream(file))
            outputStream.writeObject(map)
            outputStream.flush()
            outputStream.close()
        }

        private fun read(context: Context): TsData {
            val file = File(context.applicationInfo.dataDir, FILE_NAME)
            if (!file.exists()) {
                return TsData()
            }
            return try {
                val inputStream = ObjectInputStream(FileInputStream(file))
                val map = inputStream.readObject() as TsData
                inputStream.close()
                map
            } catch (e : Exception) {
                file.delete()
                TsData()
            }
        }

        fun clearWidgetData(context: Context, widgetId: Int) {
            val map = read(context)
            map[widgetId] = arrayListOf()
            write(context, map)
        }

        fun getWidgetCount(context: Context, widgetId: Int) : Int {
            val map = read(context)
            return map[widgetId]?.size ?: 0
        }

        fun getTsColorData(context: Context) : ArrayList<TsColorItem> {
            val data = read(context)

            val tsColorData = sortedMapOf<Long, ArrayList<Int>>()

            for (widgetId in data.keys) {
                val color = loadPref(context, widgetId, COLOR)
                if (color == 0) continue
                val timestamps = data[widgetId]
                if (timestamps != null) {
                    for (ts in timestamps) {
                        val min = ts.time / 1000 / 60
                        if (tsColorData[min] == null) {
                            tsColorData[min] = arrayListOf(color)
                        } else {
                            tsColorData[min]?.add(color)
                        }
                    }
                }
            }
            val tsColorDataCombined = arrayListOf<TsColorItem>()

            for (entry in tsColorData.entries) {
                tsColorDataCombined.add(TsColorItem(Date(entry.key*1000*60), entry.value))
            }

            return tsColorDataCombined
        }
    }
}

class HistoryDataUtil {

    companion object {
        private const val FILE_NAME = "history"
        fun addPeriod(context: Context, widgetId: Int, color: Int, count: Int) {
            if (color == 0 || count <= 0) return
            val history = read(context)
            val periodDate = normalizePeriodDate(Date())

            var summary: PeriodSummary? = null
            for (item in history) {
                if (item.date.time == periodDate.time) {
                    summary = item
                    break
                }
            }
            if (summary == null) {
                summary = PeriodSummary(periodDate, arrayListOf())
                history.add(summary)
            }

            var colorCount: PeriodColorCount? = null
            for (cc in summary.counts) {
                if (cc.widgetId == widgetId) {
                    colorCount = cc
                    break
                }
            }
            if (colorCount == null) {
                summary.counts.add(PeriodColorCount(widgetId, color, count))
            } else {
                colorCount.count += count
            }

            write(context, history)
        }

        fun getHistory(context: Context): List<PeriodSummary> {
            val history = read(context)
            return history.sortedByDescending { it.date.time }
        }

        private fun write(context: Context, history: PeriodHistory) {
            val file = File(context.applicationInfo.dataDir, FILE_NAME)
            val outputStream = ObjectOutputStream(FileOutputStream(file))
            outputStream.writeObject(history)
            outputStream.flush()
            outputStream.close()
        }

        private fun read(context: Context): PeriodHistory {
            val file = File(context.applicationInfo.dataDir, FILE_NAME)
            if (!file.exists()) {
                return PeriodHistory()
            }
            return try {
                val inputStream = ObjectInputStream(FileInputStream(file))
                val history = inputStream.readObject() as PeriodHistory
                inputStream.close()
                history
            } catch (e: Exception) {
                file.delete()
                PeriodHistory()
            }
        }

        private fun normalizePeriodDate(date: Date): Date {
            val cal = Calendar.getInstance()
            cal.time = date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }
    }
}
