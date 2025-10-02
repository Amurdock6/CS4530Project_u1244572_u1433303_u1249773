package com.example.androidracedash.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar

/**
 * A [BrightnessSlideBar] that ignores touches which begin outside its bounds.
 * Prevents accidental slider changes when users try to scroll the dialog.
 */
class SafeBrightnessSlideBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BrightnessSlideBar(context, attrs, defStyleAttr) {

    private var startedInside = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val inside =
            event.x >= 0 && event.x < width && event.y >= 0 && event.y < height
        val parent = parent
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startedInside = inside
                if (!startedInside) return false
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!startedInside) return false
                if (event.actionMasked != MotionEvent.ACTION_MOVE) {
                    startedInside = false
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
