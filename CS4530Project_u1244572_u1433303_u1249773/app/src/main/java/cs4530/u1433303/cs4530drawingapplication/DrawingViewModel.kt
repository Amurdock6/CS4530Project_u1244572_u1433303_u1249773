package cs4530.u1433303.cs4530drawingapplication

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// TODO: eventually we need images to be square (width = height), this currently just lets you draw wherever on the screen

enum class BrushShape { Round, Square }
data class Stroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val shape: BrushShape
)
data class DrawingUiState(
    val brushColor: Color = Color(0xFF000000),
    val brushSize: Float = 5f,
    val brushShape: BrushShape = BrushShape.Round,
    val strokes: List<Stroke> = emptyList()
)

class DrawingViewModel : ViewModel() {
    // create the drawing view model here
    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState = _uiState.asStateFlow()

    fun setBrushColor(color: Color) {
        _uiState.value = _uiState.value.copy(brushColor = color)
    }

    fun setBrushSize(size: Float) {
        _uiState.value = _uiState.value.copy(brushSize = size)
    }

    //TODO: create support for other pen shapes than just circle dots that are connected by lines
    fun addStroke(points: List<Offset>) {
        val s = _uiState.value
        _uiState.value = s.copy(
            strokes = s.strokes + Stroke(points, s.brushColor, s.brushSize, s.brushShape)
        )
    }

    fun setBrushShape(shape: BrushShape) {
        _uiState.value = _uiState.value.copy(brushShape = shape)
    }


    fun clearCanvas() {
        _uiState.value = _uiState.value.copy(strokes = emptyList())
    }
}