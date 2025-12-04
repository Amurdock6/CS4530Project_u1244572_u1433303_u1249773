package cs4530.u1433303.cs4530drawingapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.StrokeCap

@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    viewModel: DrawingViewModel
) {
    val state = viewModel.uiState.collectAsState()
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Canvas(
        modifier = modifier.pointerInput(true) {
            detectDragGestures(
                onDragStart = { offset ->
                    currentPath = listOf(offset)
                },
                onDrag = { change, _ ->
                    currentPath = currentPath + change.position
                },
                onDragEnd = {
                    if (currentPath.isNotEmpty()) {
                        viewModel.addStroke(currentPath)
                        currentPath = emptyList()
                    }
                }
            )
        }
    ) {
        state.value.backgroundImage?.let {
            drawImage(it.asImageBitmap())
        }

        // Draw vision labels
        if (state.value.showVisionLabels) {
            state.value.visionLabels.forEach { label ->
                label.boundingPoly?.vertices?.let { vertices ->
                    if (vertices.isNotEmpty()) {
                        val path = Path().apply {
                            moveTo(vertices.first().x, vertices.first().y)
                            vertices.drop(1).forEach { vertex ->
                                lineTo(vertex.x, vertex.y)
                            }
                            close()
                        }
                        drawPath(
                            path = path,
                            color = label.color,
                            style = Stroke(width = 5f)
                        )
                    }
                }
            }
        }

        // draw the strokes
        for (stroke in state.value.strokes) {
            val path = Path().apply {
                if (stroke.points.isNotEmpty()) {
                    moveTo(stroke.points.first().x, stroke.points.first().y)
                    stroke.points.drop(1).forEach {
                        lineTo(it.x, it.y)
                    }
                }
            }
            drawPath(
                path = path,
                color = stroke.color,
                style = Stroke(
                    width = stroke.strokeWidth,
                    cap = if (stroke.shape == BrushShape.Round) StrokeCap.Round else StrokeCap.Square
                )
            )
        }

        // drawing the current stroke so it doesn't just pop into existence when you finish
        if (currentPath.isNotEmpty()) {
            val path = Path().apply {
                moveTo(currentPath.first().x, currentPath.first().y)
                currentPath.drop(1).forEach {
                    lineTo(it.x, it.y)
                }
            }
            drawPath(
                path = path,
                color = state.value.brushColor,
                style = Stroke(
                    width = state.value.brushSize,
                    cap = if (state.value.brushShape == BrushShape.Round) StrokeCap.Round else StrokeCap.Square
                )
            )
        }
    }
}
