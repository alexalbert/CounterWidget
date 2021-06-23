package com.aa.counterwidget

import android.content.Context
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import java.io.*
import java.util.*

class TsData: HashMap<Int, ArrayList<Date>>(), Serializable{}
data class TsColorItemOld(val date: Date, val color: Int)
data class TsColorItem(val date: Date, val colors: ArrayList<Int>)

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

        private fun write(context: Context, map: TsData) {
            val file = File(context.applicationInfo.dataDir, FILE_NAME)
            val outputStream = ObjectOutputStream(FileOutputStream(file))
            outputStream.writeObject(map)
            outputStream.flush()
            outputStream.close()
        }

        fun read(context: Context): TsData {
            val file = File(context.applicationInfo.dataDir, FILE_NAME)
            if (!file.exists()) {
                return TsData()
            }
            val inputStream = ObjectInputStream(FileInputStream(file))
            val map = inputStream.readObject() as TsData
            inputStream.close()
            return map
        }

        fun clearWidgetData(context: Context, widgetId: Int) {
            val map = read(context)
            map[widgetId] = arrayListOf<Date>()
            write(context, map)
        }

        fun getTsColorDataOld(context: Context) : ArrayList<TsColorItemOld> {
            val data = read(context)

            val tsColorData = arrayListOf<TsColorItemOld>()

            for (widgetId in data.keys) {
                val color = loadPref(context, widgetId, COLOR)
                val timestamps = data[widgetId]
                if (timestamps != null) {
                    for (ts in timestamps) {
                        tsColorData.add(TsColorItemOld(ts, color))
                    }
                }
            }

            tsColorData.sortBy { ts -> ts.date }

            return tsColorData
        }

        fun getTsColorData(context: Context) : ArrayList<TsColorItem> {
            val data = read(context)

            val tsColorData = sortedMapOf<Long, ArrayList<Int>>()

            for (widgetId in data.keys) {
                val color = loadPref(context, widgetId, COLOR)
                val timestamps = data[widgetId]
                if (timestamps != null) {
                    for (ts in timestamps) {
                        val min = ts.time / 1000 / 60
                        if (tsColorData[min] == null) {
                            tsColorData[min] = arrayListOf<Int>(color)
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


