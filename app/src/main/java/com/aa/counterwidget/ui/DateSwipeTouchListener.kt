package com.aa.counterwidget.ui

import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class DateSwipeTouchListener(
    private val onSwipeLeft: () -> Unit,
    private val onSwipeRight: () -> Unit
) : View.OnTouchListener {
    private var downX = 0f
    private var downY = 0f

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                val diffX = event.x - downX
                val diffY = event.y - downY
                if (abs(diffX) > SWIPE_THRESHOLD && abs(diffX) > abs(diffY)) {
                    if (diffX < 0) {
                        onSwipeLeft()
                    } else {
                        onSwipeRight()
                    }
                    return true
                }
                view.performClick()
            }
        }
        return false
    }

    companion object {
        const val SWIPE_THRESHOLD = 80
    }
}
