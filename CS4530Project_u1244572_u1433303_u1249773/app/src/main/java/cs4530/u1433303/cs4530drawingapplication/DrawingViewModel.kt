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
import kotlin.math.min
import kotlin.random.Random
import androidx.core.graphics.scale


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

    // property to hold the pending drawing until the view is ready
    private var pendingDrawing: DrawingEntity? = null

    var currentViewWidth: Int = 1080
    var currentViewHeight: Int = 1920



    fun prepareDrawingToLoad(drawing: DrawingEntity) {
        pendingDrawing = drawing
        // Reset state immediately for a clean slate
        _uiState.value = DrawingUiState(drawingName = drawing.name)
    }

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

        val scaledBitmap = scaleBitmapToFit(bitmap, currentViewWidth, currentViewHeight)
        Log.e("DrawingViewModel", "Scaled Bitmap Width: ${scaledBitmap.width}, Height: ${scaledBitmap.height}, View Width: $currentViewWidth, Height: $currentViewHeight")

        _uiState.value = _uiState.value.copy(
            strokes = emptyList(),
            backgroundImage = scaledBitmap,
            visionLabels = emptyList(),
            drawingName = null
        )
        analyzeImage(scaledBitmap)
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

    private fun scaleBitmapToFit(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val widthRatio = maxWidth.toFloat() / bitmap.width
        val heightRatio = maxHeight.toFloat() / bitmap.height

        val scaleFactor = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
            // Image is larger than the canvas, scale it down to fit.
            min(widthRatio, heightRatio)
        } else {
            // Image is smaller than the canvas.
            if ((bitmap.height * widthRatio) > maxHeight) {
                heightRatio
            } else {
                widthRatio
            }
        }

        if (scaleFactor == 1.0f) {
            return bitmap
        }

        val newWidth = (bitmap.width * scaleFactor).toInt()
        val newHeight = (bitmap.height * scaleFactor).toInt()

        if (newWidth <= 0 || newHeight <= 0) {
            return bitmap
        }

        return bitmap.scale(newWidth, newHeight, true)
    }

    fun loadDrawing(viewWidth: Int = 1080, viewHeight: Int = 1920) {

        currentViewWidth = viewWidth;
        currentViewHeight = viewHeight;

        val drawingToLoad = pendingDrawing ?: return // Do nothing if no drawing is pending
        editing = drawingToLoad


        val scaledBitmap = scaleBitmapToFit(drawingToLoad.content, viewWidth, viewHeight)

        Log.e("DrawingViewModel", "Scaled Bitmap Width: ${scaledBitmap.width}, Height: ${scaledBitmap.height}, View Width: $currentViewWidth, Height: $currentViewHeight")

        _uiState.value = _uiState.value.copy(
            strokes = emptyList(),
            backgroundImage = scaledBitmap,
            visionLabels = emptyList(),
            drawingName = drawingToLoad.name
        )
        analyzeImage(scaledBitmap)
        Log.d("DrawingViewModel", "Drawing Content: $drawingToLoad")

        pendingDrawing = null
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

                val scaledBitmap = scaleBitmapToFit(bmp, currentViewWidth, currentViewHeight)

                _uiState.value = _uiState.value.copy(
                    strokes = emptyList(),
                    backgroundImage = scaledBitmap,
                    visionLabels = emptyList()
                )
                analyzeImage(scaledBitmap)
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
