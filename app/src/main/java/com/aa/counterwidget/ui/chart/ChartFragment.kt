package com.aa.counterwidget.ui.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aa.counterwidget.TsDataUtil
import com.aa.counterwidget.ui.Util

class ChartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ChartView(activity as Context)
    }
}

class ChartView(context: Context): View(context){

    private val BAND = 1000*60*30 // 30 min

    private var w: Int = 0
    private var h: Int = 0
    private val p = Paint()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas!!.drawColor(Util.getDefaultBackground(context))

        val tsData = TsDataUtil.getTsColorData(context)

        if (tsData.isEmpty()) return

        val start = tsData[0].date.time.toFloat()
        val end = tsData[tsData.size -1].date.time.toFloat()
        val interval = (end - start)

        var y = 0f
        var x = 0f
        var prevTs = 0L

        for(time in tsData) {
            val ts = time.date.time
            if (ts - prevTs > BAND) { // Move to next line only in the hit is in a different band
                if (interval == 0f) { // First hit
                    y = 150f
                } else {
                    y = (ts - start) / interval * (h - 300) + 150
                }
                x = 100f
                p.textSize = 34f
                p.color = Color.CYAN

                canvas.drawText(Util.formatTime(time.date), x, y+15, p)
                x = 250f
            }
            prevTs = ts

            for (color in time.colors) {
                p.color = color
                canvas. drawCircle(x, y, 10f, p )
                x += 30
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        this.h = h
        this.w = w
    }
}


