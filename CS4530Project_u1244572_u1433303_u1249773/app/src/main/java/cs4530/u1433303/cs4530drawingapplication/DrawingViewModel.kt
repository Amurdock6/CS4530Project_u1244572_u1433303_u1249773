package cs4530.u1433303.cs4530drawingapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val backgroundImage: Bitmap? = null,
    val visionLabels: List<String> = emptyList(),
    val showVisionLabels: Boolean = false
)

class DrawingViewModel(private val drawingRepository: DrawingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState = _uiState.asStateFlow()
    private var editing: DrawingEntity? = null
    private val cloudVisionRepository by lazy { CloudVisionRepository() }

    fun startNew() {
        editing = null
        clearCanvas()
    }

    fun setBrushColor(c: Color) {
        _uiState.value = _uiState.value.copy(brushColor = c)
    }

    fun setBrushSize(px: Float) {
        _uiState.value = _uiState.value.copy(brushSize = px)
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
        _uiState.value = _uiState.value.copy(
            strokes = emptyList(),
            backgroundImage = null,
            visionLabels = emptyList()
        )
    }

    fun saveDrawing(canvasView: View) {
        viewModelScope.launch {
            val existing = editing
            if (existing != null) {
                drawingRepository.updateExistingFromView(canvasView, existing)  // overwrite
            } else {
                drawingRepository.saveDrawingFromView(canvasView)               // create new
            }
        }
    }

    fun loadDrawing(drawing: DrawingEntity) {
        editing = drawing
        _uiState.value = _uiState.value.copy(
            strokes = emptyList(),
            backgroundImage = drawing.content,
            visionLabels = emptyList()
        )
        analyzeImage(drawing.content)
        Log.d("DrawingViewModel", "Drawing Content: $drawing")
    }

    fun undoLastStroke(){
        val s = _uiState.value
        _uiState.value = _uiState.value.copy(
            strokes = s.strokes.dropLast(1)
        )
    }

    fun toggleVisionLabels() {
        val s = _uiState.value
        _uiState.value = s.copy(showVisionLabels = !s.showVisionLabels)
    }

    // Import from the system Photo Picker
    fun importFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val bmp = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
            if (bmp != null) {
                _uiState.value = _uiState.value.copy(
                    strokes = emptyList(),
                    backgroundImage = bmp,
                    visionLabels = emptyList()
                )
                analyzeImage(bmp)
            }
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            val response = cloudVisionRepository.analyzeImage(bitmap)
            response?.responses?.firstOrNull()?.labelAnnotations?.let { labels ->
                val descriptions = labels.map { it.description }
                _uiState.value = _uiState.value.copy(visionLabels = descriptions)
                Log.d("DrawingViewModel", "Vision API labels: $descriptions")
            }
        }
    }
}
