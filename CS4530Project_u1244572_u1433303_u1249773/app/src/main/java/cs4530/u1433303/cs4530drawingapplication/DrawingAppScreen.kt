package cs4530.u1433303.cs4530drawingapplication

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
        val file = File(context.cacheDir, "share_${'$'}{System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${'$'}{context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share drawing"))
    }

    val appBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Canvas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleVisionLabels() }) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = "Show or hide vision labels"
                        )
                    }
                    IconButton(
                        modifier = Modifier.testTag("DrawingAppScreenUndoButton"),
                        onClick = { viewModel.undoLastStroke() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo last draw"
                        )
                    }
                    IconButton(
                        modifier = Modifier.testTag("clearCanvasButton"),
                        onClick = { viewModel.clearCanvas() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear canvas"
                        )
                    }
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
                            contentDescription = "Import image"
                        )
                    }
                    IconButton(
                        modifier = Modifier.testTag("saveCanvasButton"),
                        onClick = { canvasViewRef?.let { view -> viewModel.saveDrawing(view) } }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save drawing"
                        )
                    }
                    IconButton(
                        modifier = Modifier.testTag("shareButton"),
                        onClick = { shareCurrentCanvas() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share drawing"
                        )
                    }
                },
                colors = appBarColors
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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

                Spacer(Modifier.height(6.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                    tonalElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Brush & tools",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(state.brushColor, shape = CircleShape)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = .35f),
                                        CircleShape
                                    )
                                    .clickable { showColorDialog = true }
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Brush size: ${'$'}{state.brushSize.toInt()}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Slider(
                                    modifier = Modifier.testTag("brushSizeSlider"),
                                    value = state.brushSize,
                                    onValueChange = { viewModel.setBrushSize(it) },
                                    valueRange = 1f..60f,
                                    steps = 60 - 2
                                )
                            }

                            Spacer(Modifier.width(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    modifier = Modifier.testTag("circleBrushButton"),
                                    selected = state.brushShape == BrushShape.Round,
                                    onClick = { viewModel.setBrushShape(BrushShape.Round) },
                                    label = { Text("Round") }
                                )
                                FilterChip(
                                    modifier = Modifier.testTag("squareBrushButton"),
                                    selected = state.brushShape == BrushShape.Square,
                                    onClick = { viewModel.setBrushShape(BrushShape.Square) },
                                    label = { Text("Square") }
                                )
                            }
                        }
                    }
                }
            }

            if (state.showVisionLabels) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(14.dp),
                    tonalElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                ) {
                    LazyColumn {
                        items(state.visionLabels) { label ->
                            Text(
                                text = label,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            )
                        }
                    }
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
