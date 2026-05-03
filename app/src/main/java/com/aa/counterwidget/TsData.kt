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

        fun addTs(context: Context, widgetId: Int)  { val map = readGrouped(context)
            val color = loadPref(context, widgetId, COLOR)
            val key = if (color == 0) widgetId else color
            if (map.containsKey(key))  {
                map[key]?.add(Date())
            } else {
                map[key] = arrayListOf(Date())
            }
            write(context, map)
        }

        fun removeTs(context: Context, date: String)  {
            val map = readGrouped(context)
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
            return copyData(readGrouped(context))
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
            val map = readGrouped(context)
            val color = loadPref(context, widgetId, COLOR)
            val key = if (color == 0) widgetId else color
            map[key] = arrayListOf()
            if (key != widgetId) {
                map.remove(widgetId)
            }
            write(context, map)
        }

        fun getWidgetCount(context: Context, widgetId: Int) : Int {
            val map = readGrouped(context)
            val color = loadPref(context, widgetId, COLOR)
            val key = if (color == 0) widgetId else color
            return map[key]?.size ?: 0
        }

        fun getTsColorData(context: Context, periodDate: Date = Date()) : ArrayList<TsColorItem> {
            val normalizedDate = Util.startOfDay(periodDate)
            val data = if (Util.isSameDay(normalizedDate, Date())) {
                readGrouped(context)
            } else {
                HistoryDataUtil.getDetailedData(context, normalizedDate) ?: TsData()
            }

            val tsColorData = sortedMapOf<Long, ArrayList<Int>>()

            for (widgetId in data.keys) {
                val color = if (Util.isSameDay(normalizedDate, Date())) {
                    getCurrentColorForDataKey(context, widgetId)
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

        private fun readGrouped(context: Context): TsData {
            val map = read(context)
            if (migrateActiveWidgetDataToColors(context, map)) {
                write(context, map)
            }
            return map
        }

        private fun migrateActiveWidgetDataToColors(context: Context, map: TsData): Boolean {
            var changed = false
            for (widgetId in getWidgetIds(context)) {
                val color = loadPref(context, widgetId, COLOR)
                if (color != 0) {
                    changed = moveData(map, widgetId, color) || changed
                }
            }
            return changed
        }

        private fun moveData(map: TsData, fromKey: Int, toKey: Int): Boolean {
            if (fromKey == toKey) return false
            val oldTimestamps = map[fromKey] ?: return false
            if (oldTimestamps.isEmpty()) {
                map.remove(fromKey)
                return true
            }
            val colorTimestamps = map[toKey]
            if (colorTimestamps == null) {
                map[toKey] = oldTimestamps
            } else {
                colorTimestamps.addAll(oldTimestamps)
            }
            map.remove(fromKey)
            return true
        }

        private fun getCurrentColorForDataKey(context: Context, dataKey: Int): Int {
            val widgetColor = loadPref(context, dataKey, COLOR)
            return if (widgetColor == 0) dataKey else widgetColor
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

            val countsByColor = linkedMapOf<Int, Int>()
            for (snapshot in snapshots) {
                if (snapshot.color != 0) {
                    countsByColor[snapshot.color] = (countsByColor[snapshot.color] ?: 0) + snapshot.count
                }
            }

            for ((color, count) in countsByColor) {
                var existing = summary.counts.find { it.color == color }
                if (existing == null) {
                    existing = PeriodColorCount(color, color, count)
                    summary.counts.add(existing)
                } else {
                    existing.count = count
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
            return history
                .map { aggregateByColor(it) }
                .sortedByDescending { it.date.time }
        }

        fun getDetailedData(context: Context, periodDate: Date): TsData? {
            return readDetails(context)[normalizePeriodDate(periodDate).time]
        }

        fun getColor(context: Context, periodDate: Date, widgetId: Int): Int {
            val normalizedDate = normalizePeriodDate(periodDate)
            val counts = read(context)
                .find { it.date.time == normalizedDate.time }
                ?.counts
            val storedColor = counts
                ?.find { it.widgetId == widgetId }
                ?.color
                ?: counts
                    ?.find { it.color == widgetId }
                    ?.color
            if (storedColor != null) {
                return storedColor
            }
            val currentColor = loadPref(context, widgetId, COLOR)
            return if (currentColor == 0) widgetId else currentColor
        }

        private fun aggregateByColor(summary: PeriodSummary): PeriodSummary {
            val countsByColor = linkedMapOf<Int, Int>()
            for (count in summary.counts) {
                if (count.color != 0) {
                    countsByColor[count.color] = (countsByColor[count.color] ?: 0) + count.count
                }
            }
            return PeriodSummary(
                Date(summary.date.time),
                ArrayList(countsByColor.map { PeriodColorCount(it.key, it.key, it.value) })
            )
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
