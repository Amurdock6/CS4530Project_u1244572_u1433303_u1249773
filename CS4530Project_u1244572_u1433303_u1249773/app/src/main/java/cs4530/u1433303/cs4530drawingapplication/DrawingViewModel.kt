package cs4530.u1433303.cs4530drawingapplication

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DrawingUiState(
    val brushColor: Color = Color(0xFF000000),
    val brushSize: Float = 5f,
    // will need to add a list of drawing strokes or dots or something here
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
}