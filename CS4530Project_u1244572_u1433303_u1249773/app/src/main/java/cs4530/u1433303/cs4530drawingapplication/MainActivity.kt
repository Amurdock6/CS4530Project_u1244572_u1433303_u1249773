package cs4530.u1433303.cs4530drawingapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cs4530.u1433303.cs4530drawingapplication.ui.theme.CS4530DrawingApplicationTheme
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.testTag
import androidx.room.Room
import cs4530.u1433303.cs4530drawingapplication.data.DrawingDatabase
import cs4530.u1433303.cs4530drawingapplication.data.DrawingEntity
import cs4530.u1433303.cs4530drawingapplication.data.DrawingRepository
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            DrawingDatabase::class.java,
            "drawing_db"
        ).fallbackToDestructiveMigration().build()
        val dao = db.drawingDao()

        val drawingRepository = DrawingRepository.getInstance(applicationContext, dao)

        val viewModelFactory = ViewModelFactory(drawingRepository)

        val mainViewModel: MainViewModel by viewModels { viewModelFactory }
        val drawingViewModel: DrawingViewModel by viewModels { viewModelFactory }

        enableEdgeToEdge()
        setContent {
            CS4530DrawingApplicationTheme {
                App(mainViewModel = mainViewModel, drawingViewModel = drawingViewModel)
            }
        }
    }
}

// put the application in here basically
@Composable
fun App(mainViewModel: MainViewModel, drawingViewModel: DrawingViewModel) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            SplashScreen()
            // Navigate to main after animation delay (~1.7s total previously)
            LaunchedEffect(Unit) {
                // keep existing splash timing feel
                kotlinx.coroutines.delay(1700)
                nav.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
        composable("main") {
            MainScreen(
                viewModel = mainViewModel,
                onNewDrawing = {
                    drawingViewModel.startNew()
                    nav.navigate("editor")
                },
                onOpenDrawing = { drawing ->
                    drawingViewModel.loadDrawing(drawing)
                    nav.navigate("editor")
                }
            )
        }
        composable("editor") {
            DrawingScreen(
                viewModel = drawingViewModel,
                onBack = { nav.popBackStack() }
            )
        }
    }
}

@Composable
fun DrawingScreen(viewModel: DrawingViewModel, onBack: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        DrawingAppScreen(
            viewModel,
            onBack = onBack
        )
    }}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNewDrawing: () -> Unit,
    onOpenDrawing: (DrawingEntity) -> Unit
) {
    val drawings by viewModel.drawings.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "My Drawings",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(drawings) { drawing ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDrawing(drawing) }
                        .padding(16.dp)
                        .testTag(drawing.name), // For espresso testing, the drawing Name is used as a tag
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = drawing.name)
                    IconButton(onClick = { viewModel.deleteDrawing(drawing) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }

        Button(
            onClick = onNewDrawing,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Text("New Drawing")
        }
    }
}


@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,

    )
    {
        Image (
            painter = painterResource(id = R.drawable.splash_screen_image),
            contentDescription = "Splash logo image"
        )
    }
}