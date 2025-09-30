package cs4530.u1433303.cs4530drawingapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class) // using this for TopAppBar, API is stable enough
// but we probably should refactor at some point to avoid this and just use row and column stuffs
@Composable
fun DrawingAppScreen(viewModel: DrawingViewModel) {
    val state = viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Drawing App") },
                actions = {
                    IconButton(onClick = { viewModel.clearCanvas() }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Canvas"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            DrawingCanvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                viewModel = viewModel
            )

            // do brush controls (right now they're dumb just 2 buttons)
            // TODO: fix brush controls to make them better than just the 2 buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = { viewModel.setBrushColor(Color.Red) }) {
                    Text("Red")
                }
                Button(onClick = { viewModel.setBrushSize(state.value.brushSize + 5f) }) {
                    Text("Bigger Brush")
                }
            }
        }
    }
}
