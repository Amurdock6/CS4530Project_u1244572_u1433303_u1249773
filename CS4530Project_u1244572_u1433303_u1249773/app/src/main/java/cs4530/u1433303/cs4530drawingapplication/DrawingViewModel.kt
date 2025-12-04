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
import cs4530.u1433303.cs4530drawingapplication.BoundingPoly
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class BrushShape { Round, Square }

data class Stroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val shape: BrushShape
)

data class UiBoundingPoly(val vertices: List<Offset>)

data class VisionLabel(
    val description: String,
    val boundingPoly: UiBoundingPoly?,
    val color: Color
)

data class DrawingUiState(
    val brushColor: Color = Color(0xFF000000),
    val brushSize: Float = 5f,
    val brushShape: BrushShape = BrushShape.Round,
    val strokes: List<Stroke> = emptyList(),
    val backgroundImage: Bitmap? = null,
    val visionLabels: List<VisionLabel> = emptyList(),
    val showVisionLabels: Boolean = false,
    var drawingName: String? = null
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
            drawingName = null
        )
    }

    fun loadImportedBitmap(bitmap: Bitmap) {
        editing = null
        _uiState.value = _uiState.value.copy(
            strokes = emptyList(),
            backgroundImage = bitmap,
            visionLabels = emptyList(),
            drawingName = null
        )
        analyzeImage(bitmap)
    }

    fun saveDrawing(canvasView: View, drawingName: String? = _uiState.value.drawingName) {
        viewModelScope.launch {
            val existing = editing
            if (existing != null) {
                drawingRepository.updateExistingFromView(canvasView, existing)  // overwrite
            } else {
                if (drawingName != null) {
                    drawingRepository.saveDrawingFromView(canvasView, drawingName)
                }
                else {
                    drawingRepository.saveDrawingFromView(canvasView)
                }
            }
        }
    }

    fun loadDrawing(drawing: DrawingEntity) {
        editing = drawing
        _uiState.value = _uiState.value.copy(
            strokes = emptyList(),
            backgroundImage = drawing.content,
            visionLabels = emptyList(),
            drawingName = drawing.name
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
            val visionLabels = mutableListOf<VisionLabel>()
            val imageWidth = bitmap.width
            val imageHeight = bitmap.height
            response?.responses?.firstOrNull()?.let { res ->
                res.labelAnnotations?.let { labels ->
                    visionLabels.addAll(
                        labels.mapNotNull {
                            convertBoundingPoly(it.boundingPoly, imageWidth, imageHeight)?.let { uiPoly ->
                                VisionLabel(it.description, uiPoly, randomColor())
                            }
                        }
                    )
                }
                res.localizedObjectAnnotations?.let { objects ->
                    visionLabels.addAll(
                        objects.mapNotNull {
                            convertBoundingPoly(it.boundingPoly, imageWidth, imageHeight)?.let { uiPoly ->
                                VisionLabel(it.name, uiPoly, randomColor())
                            }
                        }
                    )
                }
            }
            _uiState.value = _uiState.value.copy(visionLabels = visionLabels)
            Log.d("DrawingViewModel", "Vision API labels: $visionLabels")
        }
    }

    private fun convertBoundingPoly(poly: BoundingPoly?, imageWidth: Int, imageHeight: Int): UiBoundingPoly? {
        return poly?.let { p ->
            val mappedVertices = p.normalizedVertices.map { v ->
                Offset( (v.x ?: 0f) * imageWidth, (v.y ?: 0f) * imageHeight)
            }
            UiBoundingPoly(mappedVertices)
        }
    }

    private fun randomColor(): Color {
        val random = Random.Default
        return Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }
}
