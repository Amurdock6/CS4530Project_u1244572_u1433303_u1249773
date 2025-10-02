package com.example.androidracedash.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.skydoves.colorpickerview.sliders.AlphaSlideBar

/**
 * An [AlphaSlideBar] that ignores touches starting outside its bounds so
 * scrolling the surrounding dialog doesn't adjust transparency.
 */
class SafeAlphaSlideBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AlphaSlideBar(context, attrs, defStyleAttr) {

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
