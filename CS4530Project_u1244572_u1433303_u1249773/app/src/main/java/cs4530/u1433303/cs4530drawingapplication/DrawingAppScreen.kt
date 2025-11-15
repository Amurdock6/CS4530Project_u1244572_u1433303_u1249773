package cs4530.u1433303.cs4530drawingapplication

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import java.io.File
import java.io.FileOutputStream


@OptIn(ExperimentalMaterial3Api::class) // using this for TopAppBar, API is stable enough
@Composable
fun DrawingAppScreen(viewModel: DrawingViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var showColorDialog by remember { mutableStateOf(false) }
    var canvasViewRef by remember { mutableStateOf<ComposeView?>(null) }
    var labelsExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Photo Picker launcher (imports an image as background)
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.importFromUri(context, uri)
        }
    }

    // Helper: share current canvas as a PNG via FileProvider
    fun shareCurrentCanvas() {
        val view = canvasViewRef ?: return
        val bmp = view.drawToBitmap()
        val file = File(context.cacheDir, "share_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share drawing"))
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drawing App") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Vision Labels Toggle
                    IconButton(
                        onClick = { viewModel.toggleVisionLabels(); labelsExpanded = false }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Show/Hide Vision Labels"
                        )
                    }
                    // Small count indicator for labels
                    if (state.visionLabels.isNotEmpty()) {
                        Text("${state.visionLabels.size}")
                    }
                    // Undo
                    IconButton(
                        modifier = Modifier.testTag("DrawingAppScreenUndoButton"),
                        onClick = {viewModel.undoLastStroke()}
                    ){
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo Last Draw"
                        )
                    }
                    // Clear
                    IconButton(
                        modifier = Modifier.testTag("clearCanvasButton"),
                        onClick = { viewModel.clearCanvas() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Canvas"
                        )
                    }

                    // Import (Photo Picker)
                    IconButton(
                        modifier = Modifier.testTag("importButton"),
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Import Image"
                        )
                    }

                    // Save (to Room + filesDir via repo)
                    IconButton(
                        modifier = Modifier.testTag("saveCanvasButton"),
                        onClick = { canvasViewRef?.let { view -> viewModel.saveDrawing(view) } }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save Drawing"
                        )
                    }

                    // Share (export to cache + FileProvider)
                    IconButton(
                        modifier = Modifier.testTag("shareButton"),
                        onClick = { shareCurrentCanvas() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Drawing"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Canvas
                AndroidView(
                    factory = { ctx ->
                        ComposeView(ctx).apply {
                            setContent {
                                DrawingCanvas(viewModel = viewModel)
                            }
                            canvasViewRef = this
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("drawCanvas")
                )

                // --- Pen bar ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color preview (tap to open dialog)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(state.brushColor, shape = CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = .4f), CircleShape)
                            .clickable { showColorDialog = true }
                    )

                    Spacer(Modifier.width(12.dp))

                    // Size slider
                    Column(Modifier.weight(1f)) {
                        Text("Size: ${state.brushSize.toInt()}")
                        Slider(
                            modifier = Modifier.testTag("brushSizeSlider"),
                            value = state.brushSize,
                            onValueChange = { viewModel.setBrushSize(it) },
                            valueRange = 1f..60f,   // tune as we see fit
                            steps = 60 - 2
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Shape: Round | Square (simple 2-state chips)
                    Row {
                        FilterChip(
                            modifier = Modifier.testTag("circleBrushButton"),
                            selected = state.brushShape == BrushShape.Round,
                            onClick = { viewModel.setBrushShape(BrushShape.Round) },
                            label = { Text("â—‹") }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            modifier = Modifier.testTag("squareBrushButton"),
                            selected = state.brushShape == BrushShape.Square,
                            onClick = { viewModel.setBrushShape(BrushShape.Square) },
                            label = { Text("â–¢") }
                        )
                    }
                }
            }
            if (state.showVisionLabels) {
                // Compact HUD instead of full drop-down list
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.material3.Surface(
                        tonalElevation = 6.dp,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "AI: ${state.visionObjects.size} objs - ${state.visionLabels.size} labels",
                                style = MaterialTheme.typography.bodyMedium
                            )
                                                        val shownLabels = if (labelsExpanded) state.visionLabels else state.visionLabels.take(3)
                            if (shownLabels.isNotEmpty()) {
                                Spacer(Modifier.width(4.dp))
                                shownLabels.forEach { lbl ->
                                    Text(text = "- $lbl", style = MaterialTheme.typography.bodySmall)
                                }
                                if (state.visionLabels.size > 3) {
                                    val remaining = state.visionLabels.size - 3
                                    if (!labelsExpanded) {
                                        Text(
                                            text = "+$remaining more",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.clickable { labelsExpanded = true }
                                        )
                                    } else {
                                        Text(
                                            text = "Show less",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.clickable { labelsExpanded = false }
                                        )
                                    }
                                }
                            }
                            if (state.visionObjects.isEmpty() && state.visionLabels.isEmpty() && state.visionMessage != null) {
                                Text(text = state.visionMessage ?: "No detections", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            // Loading overlay
            if (state.isAnalyzing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showColorDialog) {
        ColorPickerDialog(
            initial = state.brushColor,
            onConfirm = { c -> viewModel.setBrushColor(c); showColorDialog = false },
            onDismiss = { showColorDialog = false }
        )
    }
}




