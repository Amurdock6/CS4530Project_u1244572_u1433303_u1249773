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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import cs4530.u1433303.cs4530drawingapplication.ui.theme.CS4530DrawingApplicationTheme
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private val drawingViewModel: DrawingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CS4530DrawingApplicationTheme {
                App(drawingViewModel)
            }
        }
    }
}

// put the application in here basically
@Composable
fun App(viewModel: DrawingViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    //TODO: currently the app forgets its done with the splash screen when it rotates,
    // need to make that part of a viewModel and make it persist past rotations
    var splashDone by remember { mutableStateOf(false) }
    var currentlyDrawing by remember { mutableStateOf(false)}

    LaunchedEffect(Unit) {
        delay(900)
        showSplash = false

        delay(800)
        splashDone = true
    }

    when {
        showSplash || !splashDone -> {
            Box(modifier = Modifier.fillMaxSize()) {

                AnimatedVisibility(visible = showSplash, exit = fadeOut(animationSpec = tween(1000)))
                {
                    SplashScreen()
                }
            }
        }
        //TODO: create main screen to load files from and go there first before opening a drawing
    // - commented out having the main screen where file loading and such happens
    // will also need the currentlyDrawing to persist in a viewModel so we
    // don't open the file menu every time your screen rotates\

//        splashDone && !currentlyDrawing -> {
//            MainScreen()
//        }
        splashDone /*&& currentlyDrawing*/ -> {
            DrawingScreen(viewModel)
        }
    }
}

@Composable
fun DrawingScreen(viewModel: DrawingViewModel) {
    Surface(modifier = Modifier.fillMaxSize()) {
        DrawingAppScreen(viewModel)
    }}

//TODO: file opening manager screen type of thing goes here.
@Composable
fun MainScreen() {
    // main screen here -> menu to open drawings maybe? or the drawing page perhaps
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(text = "Main Screen")
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    )
    {
        // TODO: replace splash image with something nicer looking (white default on white background currently lmao)
        // insert splash image here - currently uses the 'default' ic_launcher_foreground
        Image (
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Splash logo image"
        )
    }
}