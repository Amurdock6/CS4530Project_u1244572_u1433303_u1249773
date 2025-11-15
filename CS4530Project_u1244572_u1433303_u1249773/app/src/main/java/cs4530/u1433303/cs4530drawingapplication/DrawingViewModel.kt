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
    val showVisionLabels: Boolean = false,
    val visionObjects: List<DetectedObject> = emptyList(),
    val isAnalyzing: Boolean = false,
    val visionMessage: String? = null
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
            visionLabels = emptyList(),
            visionObjects = emptyList(),
            isAnalyzing = false,
            visionMessage = null
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
            visionLabels = emptyList(),
            visionObjects = emptyList(),
            visionMessage = null
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
                    visionLabels = emptyList(),
                    visionObjects = emptyList(),
                    visionMessage = null
                )
                analyzeImage(bmp)
            }
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            // show loading
            _uiState.value = _uiState.value.copy(isAnalyzing = true, visionMessage = null)

            try {
                val response = cloudVisionRepository.analyzeImage(bitmap)

                var descriptions: List<String> = emptyList()
                var detected = emptyList<DetectedObject>()

                response?.responses?.firstOrNull()?.labelAnnotations?.let { labels ->
                    descriptions = labels.map { it.description }
                    Log.d("DrawingViewModel", "Vision API labels: $descriptions")
                }

                response?.responses?.firstOrNull()?.localizedObjectAnnotations?.let { objs ->
                    val w = bitmap.width.toFloat()
                    val h = bitmap.height.toFloat()

                    val palette = listOf(
                        Color(0xFFE53935), Color(0xFFD81B60), Color(0xFF8E24AA),
                        Color(0xFF5E35B1), Color(0xFF3949AB), Color(0xFF1E88E5),
                        Color(0xFF00897B), Color(0xFF43A047), Color(0xFFFDD835),
                        Color(0xFFFB8C00)
                    )

                    detected = objs.mapIndexed { idx, o ->
                        val verts = o.boundingPoly.normalizedVertices.map { v ->
                            Offset((v.x ?: 0f) * w, (v.y ?: 0f) * h)
                        }
                        DetectedObject(
                            name = o.name,
                            score = o.score,
                            vertices = verts,
                            color = palette[idx % palette.size]
                        )
                    }
                }

                val message = if (response == null) {
                    "Vision request failed"
                } else if (descriptions.isEmpty() && detected.isEmpty()) {
                    "No detections"
                } else null

                _uiState.value = _uiState.value.copy(
                    visionLabels = descriptions,
                    visionObjects = detected,
                    visionMessage = message
                )
            } finally {
                _uiState.value = _uiState.value.copy(isAnalyzing = false)
            }
        }
    }
}

data class DetectedObject(
    val name: String,
    val score: Float,
    val vertices: List<Offset>,
    val color: Color
)
