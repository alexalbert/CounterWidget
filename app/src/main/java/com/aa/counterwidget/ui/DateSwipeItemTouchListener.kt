package com.aa.counterwidget.ui

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class DateSwipeItemTouchListener(
    private val onSwipeLeft: () -> Unit,
    private val onSwipeRight: () -> Unit
) : RecyclerView.SimpleOnItemTouchListener() {
    private var downX = 0f
    private var downY = 0f

    override fun onInterceptTouchEvent(rv: RecyclerView, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
            }
            MotionEvent.ACTION_UP -> {
                val diffX = event.x - downX
                val diffY = event.y - downY
                if (abs(diffX) > DateSwipeTouchListener.SWIPE_THRESHOLD && abs(diffX) > abs(diffY)) {
                    if (diffX < 0) {
                        onSwipeLeft()
                    } else {
                        onSwipeRight()
                    }
                    return true
                }
            }
        }
        return false
    }
}
