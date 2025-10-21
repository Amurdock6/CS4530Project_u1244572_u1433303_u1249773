package cs4530.u1433303.cs4530drawingapplication

import android.graphics.Bitmap
import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cs4530.u1433303.cs4530drawingapplication.data.DrawingDao
import cs4530.u1433303.cs4530drawingapplication.data.DrawingEntity
import cs4530.u1433303.cs4530drawingapplication.data.DrawingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    val strokes: List<Stroke> = emptyList(),
    val backgroundImage: Bitmap? = null
)

class DrawingViewModel(private val repository: DrawingRepository, private val dao: DrawingDao) : ViewModel() {
    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState = _uiState.asStateFlow()
    var width = 1080;
    var height = 1920;

    fun setBrushColor(color: Color) {
        _uiState.value = _uiState.value.copy(brushColor = color)
    }

    fun setBrushSize(size: Float) {
        _uiState.value = _uiState.value.copy(brushSize = size)
    }

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

    // TODO: somewhere in here we should have it so if you load a file then save it, it will overwrite the currently open file instead of making a new copy
    fun saveDrawing(canvasView: View) {
        viewModelScope.launch {
            repository.saveDrawingFromView(canvasView, dao)
        }
    }

    fun loadDrawing(drawing: DrawingEntity) {
        clearCanvas()
        _uiState.value = _uiState.value.copy(backgroundImage = drawing.content)
    }
}