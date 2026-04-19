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
class PeriodTsHistory : HashMap<Long, TsData>(), Serializable

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

        fun snapshot(context: Context): TsData {
            return copyData(read(context))
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

        fun getTsColorData(context: Context, periodDate: Date = Date()) : ArrayList<TsColorItem> {
            val normalizedDate = Util.startOfDay(periodDate)
            val data = if (Util.isSameDay(normalizedDate, Date())) {
                read(context)
            } else {
                HistoryDataUtil.getDetailedData(context, normalizedDate) ?: TsData()
            }

            val tsColorData = sortedMapOf<Long, ArrayList<Int>>()

            for (widgetId in data.keys) {
                val color = if (Util.isSameDay(normalizedDate, Date())) {
                    loadPref(context, widgetId, COLOR)
                } else {
                    HistoryDataUtil.getColor(context, normalizedDate, widgetId)
                }
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

        private fun copyData(data: TsData): TsData {
            val copy = TsData()
            for ((widgetId, timestamps) in data) {
                copy[widgetId] = ArrayList(timestamps.map { Date(it.time) })
            }
            return copy
        }
    }
}

class HistoryDataUtil {

    companion object {
        private const val FILE_NAME = "history"
        private const val DETAIL_FILE_NAME = "history_details"

        fun addDailySnapshot(
            context: Context,
            periodDate: Date,
            snapshots: List<PeriodColorCount>,
            detailedData: TsData? = null
        ) {
            val history = read(context)
            val normalizedDate = normalizePeriodDate(periodDate)

            var summary = history.find { it.date.time == normalizedDate.time }
            if (summary == null) {
                summary = PeriodSummary(normalizedDate, arrayListOf())
                history.add(summary)
            }

            for (snapshot in snapshots) {
                if (snapshot.color == 0) continue
                var existing = summary.counts.find { it.widgetId == snapshot.widgetId }
                if (existing == null) {
                    existing = PeriodColorCount(snapshot.widgetId, snapshot.color, snapshot.count)
                    summary.counts.add(existing)
                } else {
                    existing.count = snapshot.count
                }
                if (existing.color != snapshot.color) {
                    summary.counts.removeIf { it.widgetId == snapshot.widgetId }
                    summary.counts.add(PeriodColorCount(snapshot.widgetId, snapshot.color, snapshot.count))
                }
            }

            write(context, history)

            if (detailedData != null) {
                val detailHistory = readDetails(context)
                detailHistory[normalizedDate.time] = detailedData
                writeDetails(context, detailHistory)
            }
        }

        fun getHistory(context: Context): List<PeriodSummary> {
            val history = read(context)
            return history.sortedByDescending { it.date.time }
        }

        fun getDetailedData(context: Context, periodDate: Date): TsData? {
            return readDetails(context)[normalizePeriodDate(periodDate).time]
        }

        fun getColor(context: Context, periodDate: Date, widgetId: Int): Int {
            val normalizedDate = normalizePeriodDate(periodDate)
            return read(context)
                .find { it.date.time == normalizedDate.time }
                ?.counts
                ?.find { it.widgetId == widgetId }
                ?.color
                ?: loadPref(context, widgetId, COLOR)
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

        private fun writeDetails(context: Context, history: PeriodTsHistory) {
            val file = File(context.applicationInfo.dataDir, DETAIL_FILE_NAME)
            val outputStream = ObjectOutputStream(FileOutputStream(file))
            outputStream.writeObject(history)
            outputStream.flush()
            outputStream.close()
        }

        private fun readDetails(context: Context): PeriodTsHistory {
            val file = File(context.applicationInfo.dataDir, DETAIL_FILE_NAME)
            if (!file.exists()) {
                return PeriodTsHistory()
            }
            return try {
                val inputStream = ObjectInputStream(FileInputStream(file))
                val history = inputStream.readObject() as PeriodTsHistory
                inputStream.close()
                history
            } catch (e: Exception) {
                file.delete()
                PeriodTsHistory()
            }
        }

        private fun normalizePeriodDate(date: Date): Date {
            return Util.startOfDay(date)
        }
    }
}
