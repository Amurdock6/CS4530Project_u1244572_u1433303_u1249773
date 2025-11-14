package cs4530.u1433303.cs4530drawingapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.material.textfield.TextInputEditText
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorListener
import com.skydoves.colorpickerview.sliders.AlphaSlideBar
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar

/**
 * Composable for a color picker.
 * Features a radial color-picker, sliders for opacity and brightness.
 */
@Composable
fun ColorPickerDialog(
    initial: Color,
    onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(initial) }

    Dialog(
        onDismissRequest = onDismiss,
        // Let us control the width instead of the platform wrapping to content
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = 360.dp, max = 600.dp), // force min width, cap max
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AndroidView<View>(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx: Context ->
                        val root = LayoutInflater.from(ctx)
                            .inflate(R.layout.dialog_color_picker, null, false).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            }

                        val picker: ColorPickerView = root.findViewById(R.id.colorPickerView)
                        val bSlider: BrightnessSlideBar = root.findViewById(R.id.brightnessSlideBar)
                        val aSlider: AlphaSlideBar = root.findViewById(R.id.alphaSlideBar)
                        val preview: View = root.findViewById(R.id.previewColorView)
                        val etHex: TextInputEditText = root.findViewById(R.id.etHex)
                        val brightnessLabel: android.widget.TextView = root.findViewById(R.id.brightnessLabel)
                        val alphaLabel: android.widget.TextView = root.findViewById(R.id.alphaLabel)

                        fun updatePercentLabels(colorInt: Int) {
                            // Brightness (HSV value) and Alpha in %
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV(colorInt, hsv)
                            val brightPct = (hsv[2] * 100f).toInt()
                            val alphaPct = (android.graphics.Color.alpha(colorInt) * 100f / 255f).toInt()
                            brightnessLabel.text = "B: $brightPct%"
                            alphaLabel.text = "A: $alphaPct%"
                        }

                        var suppressHexWatcher = false
                        fun setHexField(colorInt: Int) {
                            suppressHexWatcher = true
                            etHex.setText("#%08X".format(colorInt))
                            etHex.setSelection(etHex.text?.length ?: 0)
                            suppressHexWatcher = false
                        }

                        fun applyColor(colorInt: Int) {
                            selected = Color(colorInt)
                            setHexField(colorInt)
                            updatePercentLabels(colorInt)
                            preview.setBackgroundColor(colorInt)
                            preview.alpha = android.graphics.Color.alpha(colorInt) / 255f

                            // Optionally move the wheel/rails to this color too:
                            picker.setInitialColor(colorInt)

                        }

                        picker.attachBrightnessSlider(bSlider)
                        picker.attachAlphaSlider(aSlider)

                        // Keep labels & HEX in sync while picking
                        picker.setColorListener(
                            ColorListener { color, _ ->
                                preview.setBackgroundColor(color)
                                selected = Color(color)
                                setHexField(color)
                                updatePercentLabels(color)
                            }
                        )

                        // Parse HEX as the user types: #RRGGBB or #AARRGGBB
                        etHex.addTextChangedListener(object : android.text.TextWatcher {
                            override fun afterTextChanged(s: android.text.Editable?) {
                                if (suppressHexWatcher) return
                                val t = s?.toString()?.trim().orEmpty()
                                if (t.isEmpty()) return

                                val candidate = if (t.startsWith("#")) t else "#$t"
                                val colorInt = try {
                                    // only accept #RRGGBB or #AARRGGBB
                                    if (candidate.length == 7 || candidate.length == 9)
                                        android.graphics.Color.parseColor(candidate)
                                    else return
                                } catch (_: IllegalArgumentException) { return }

                                applyColor(colorInt)
                            }
                            override fun beforeTextChanged(cs: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {}
                        })

                        root
                    }
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { onConfirm(selected) }) { Text("OK") }
                }
            }
        }
    }
}
