package cs4530.u1433303.cs4530drawingapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Divider
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
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.sliders.AlphaSlideBar
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar

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
                            .inflate(R.layout.dialog_color_picker, null, false)

                        // Make sure the Android root takes the dialog's full width
                        root.layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        val picker: ColorPickerView = root.findViewById(R.id.colorPickerView)
                        val bSlider: BrightnessSlideBar = root.findViewById(R.id.brightnessSlideBar)
                        val aSlider: AlphaSlideBar = root.findViewById(R.id.alphaSlideBar)
                        val preview: View = root.findViewById(R.id.previewColorView)
                        val etHex: com.google.android.material.textfield.TextInputEditText =
                            root.findViewById(R.id.etHex)

                        val argb = android.graphics.Color.argb(
                            (initial.alpha * 255).toInt(),
                            (initial.red * 255).toInt(),
                            (initial.green * 255).toInt(),
                            (initial.blue * 255).toInt()
                        )
                        picker.attachBrightnessSlider(bSlider)
                        picker.attachAlphaSlider(aSlider)
                        picker.setInitialColor(argb)
                        preview.setBackgroundColor(argb)
                        etHex.setText("#%08X".format(argb))

                        picker.setColorListener(
                            com.skydoves.colorpickerview.listeners.ColorEnvelopeListener { envelope, _ ->
                                val c = envelope.color
                                preview.setBackgroundColor(c)
                                etHex.setText("#%08X".format(c))
                                selected = Color(c)
                            }
                        )
                        root
                    }
                )

                Divider()

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
