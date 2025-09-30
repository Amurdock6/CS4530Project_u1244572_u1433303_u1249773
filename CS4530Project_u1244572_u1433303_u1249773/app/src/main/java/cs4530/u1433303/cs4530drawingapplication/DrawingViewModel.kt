package cs4530.u1433303.cs4530drawingapplication

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// TODO: eventually we need images to be square (width = height), this currently just lets you draw wherever on the screen
data class Stroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)
data class DrawingUiState(
    val brushColor: Color = Color(0xFF000000),
    val brushSize: Float = 5f,
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
        val stroke = Stroke(points, _uiState.value.brushColor, _uiState.value.brushSize)
        _uiState.value = _uiState.value.copy(
            strokes = _uiState.value.strokes + stroke
        )
    }

    fun clearCanvas() {
        _uiState.value = _uiState.value.copy(strokes = emptyList())
    }
}