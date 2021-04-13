package com.aa.counterwidget

import android.content.Context
import java.io.File

data class Snapshot(var day: Int, var count: Int) {
    override fun toString(): String {
        return day.toString() + "," + count.toString()
    }

    companion object {
        fun fromString(s: String): Snapshot {
            val tokens = s.split(",")
            return Snapshot(tokens[0].toInt(), tokens[1].toInt())
        }
    }
}

class Persistence {
    companion object {
        fun write(context: Context, id: String, snapshot: Snapshot) {
            val f = File( context.cacheDir, id)
            f.writeText(snapshot.toString())
        }

        fun read(context: Context, id: String) : Snapshot {
            val f = File( context.cacheDir, id)
            val s = f.readText()
            return Snapshot.fromString(s)
        }
    }
}