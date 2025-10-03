package cs4530.u1433303.cs4530drawingapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class) // using this for TopAppBar, API is stable enough
// but we probably should refactor at some point to avoid this and just use row and column stuffs
@Composable
fun DrawingAppScreen(viewModel: DrawingViewModel) {
    val state = viewModel.uiState.collectAsState()
    var showColorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drawing App") },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("clearCanvasButton"),
                        onClick = { viewModel.clearCanvas() }
                    )
                    {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Canvas"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            // Canvas
            DrawingCanvas(
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("drawCanvas"),
                viewModel = viewModel
            )

            // --- Pen bar (like your mock) ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color preview (tap to open dialog)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(state.value.brushColor, shape = CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .4f), CircleShape)
                        .clickable { showColorDialog = true }
                )

                Spacer(Modifier.width(12.dp))

                // Size slider
                Column(Modifier.weight(1f)) {
                    Text("Size: ${state.value.brushSize.toInt()}")
                    Slider(
                        modifier = Modifier.testTag("brushSizeSlider"),
                        value = state.value.brushSize,
                        onValueChange = { viewModel.setBrushSize(it) },
                        valueRange = 1f..60f,   // tune as you like
                        steps = 60 - 2
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Shape: Round | Square (simple 2-state chips)
                Row {
                    FilterChip(
                        modifier = Modifier.testTag("circleBrushButton"),
                        selected = state.value.brushShape == BrushShape.Round,
                        onClick = { viewModel.setBrushShape(BrushShape.Round) },
                        label = { Text("○") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        modifier = Modifier.testTag("squareBrushButton"),
                        selected = state.value.brushShape == BrushShape.Square,
                        onClick = { viewModel.setBrushShape(BrushShape.Square) },
                        label = { Text("▢") }
                    )
                }
            }
        }
    }

    if (showColorDialog) {
        ColorPickerDialog(
            initial = state.value.brushColor,
            onConfirm = { c -> viewModel.setBrushColor(c); showColorDialog = false },
            onDismiss = { showColorDialog = false }
        )
    }
}