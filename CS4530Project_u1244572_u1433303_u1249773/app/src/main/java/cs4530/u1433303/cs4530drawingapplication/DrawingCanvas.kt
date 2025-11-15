package cs4530.u1433303.cs4530drawingapplication

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.nativeCanvas

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

        // Draw AI object overlays (polygons) when toggled on
        if (state.value.showVisionLabels && state.value.visionObjects.isNotEmpty()) {
            state.value.visionObjects.forEach { obj ->
                val pts = obj.vertices
                if (pts.isNotEmpty()) {
                    val poly = Path().apply {
                        moveTo(pts.first().x, pts.first().y)
                        pts.drop(1).forEach { lineTo(it.x, it.y) }
                        close()
                    }
                    // Semi-transparent fill
                    drawPath(poly, color = obj.color.copy(alpha = 0.18f))
                    // Solid outline
                    drawPath(poly, color = obj.color, style = Stroke(width = 3f))

                    // Label with confidence near the top-left of the polygon
                    val minX = pts.minOf { it.x }
                    val minY = pts.minOf { it.y }
                    val label = "${obj.name} ${(obj.score * 100).toInt()}%"

                    drawIntoCanvas { canvas ->
                        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = android.graphics.Color.WHITE
                            textSize = 28f
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        }
                        val bgPadding = 8f
                        val textWidth = textPaint.measureText(label)
                        val fm = textPaint.fontMetrics
                        val textHeight = fm.bottom - fm.top
                        val bgLeft = minX
                        val bgTop = max(0f, minY - textHeight - bgPadding * 2)
                        val bgRight = bgLeft + textWidth + bgPadding * 2
                        val bgBottom = bgTop + textHeight + bgPadding * 2

                        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = obj.color.copy(alpha = 0.85f).toArgb()
                            style = Paint.Style.FILL
                        }

                        canvas.nativeCanvas.drawRect(bgLeft, bgTop, bgRight, bgBottom, bgPaint)
                        val textX = bgLeft + bgPadding
                        val textY = bgTop + bgPadding - fm.top // baseline so text fits in rect
                        canvas.nativeCanvas.drawText(label, textX, textY, textPaint)
                    }
                }
            }
        }
    }
}
