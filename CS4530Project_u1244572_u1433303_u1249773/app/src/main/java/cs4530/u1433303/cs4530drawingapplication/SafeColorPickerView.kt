package com.example.androidracedash.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import com.skydoves.colorpickerview.ColorPickerView

/**
 * A [ColorPickerView] that ignores touches which begin outside the circular
 * color wheel. This prevents accidental colour changes when users try to
 * scroll the surrounding dialog.
 */
class SafeColorPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ColorPickerView(context, attrs, defStyleAttr) {

    private var startedInside = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy)
        val dx = event.x - cx
        val dy = event.y - cy
        val inside = dx * dx + dy * dy <= radius * radius

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startedInside = inside
                if (!startedInside) return false
                // If the touch starts inside the wheel, prevent the parent
                // ScrollView from stealing our events so the user can drag
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!startedInside) return false
                if (event.actionMasked != MotionEvent.ACTION_MOVE) {
                    startedInside = false
                    // Allow the parent to intercept again once the gesture ends
                    parent.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
