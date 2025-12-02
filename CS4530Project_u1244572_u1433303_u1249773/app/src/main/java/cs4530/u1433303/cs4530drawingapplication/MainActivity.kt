package cs4530.u1433303.cs4530drawingapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import cs4530.u1433303.cs4530drawingapplication.data.DrawingDatabase
import cs4530.u1433303.cs4530drawingapplication.data.DrawingEntity
import cs4530.u1433303.cs4530drawingapplication.data.DrawingRepository
import cs4530.u1433303.cs4530drawingapplication.ui.theme.CS4530DrawingApplicationTheme
import kotlinx.coroutines.delay

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
        val authViewModel: AuthViewModel by viewModels()

        enableEdgeToEdge()
        setContent {
            CS4530DrawingApplicationTheme {
                App(
                    mainViewModel = mainViewModel,
                    drawingViewModel = drawingViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

// put the application in here basically
@Composable
fun App(mainViewModel: MainViewModel, drawingViewModel: DrawingViewModel, authViewModel: AuthViewModel) {
    val nav = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") {
            SplashScreen()
            // Navigate to auth/main after animation delay (~1.7s total previously)
            LaunchedEffect(authState.user) {
                delay(1700)
                nav.navigate(if (authState.user != null) "main" else "auth") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
        composable("auth") {
            val state by authViewModel.uiState.collectAsState()
            LaunchedEffect(state.user) {
                if (state.user != null) {
                    nav.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            }
            AuthScreen(
                state = state,
                onEmailChange = authViewModel::updateEmail,
                onPasswordChange = authViewModel::updatePassword,
                onSignIn = authViewModel::signIn,
                onSignUp = authViewModel::signUp
            )
        }
        composable("main") {
            MainScreen(
                viewModel = mainViewModel,
                userEmail = authState.user?.email ?: "Unknown user",
                onSignOut = {
                    authViewModel.signOut()
                    nav.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                },
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
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    userEmail: String,
    onSignOut: () -> Unit,
    onNewDrawing: () -> Unit,
    onOpenDrawing: (DrawingEntity) -> Unit
) {
    val drawings by viewModel.drawings.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "My Drawings",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onSignOut) {
                Text("Sign Out")
            }
        }

        Divider()

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(drawings) { drawing ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDrawing(drawing) }
                        .padding(16.dp)
                        .testTag(drawing.name), // For espresso testing, the drawing Name is used as a tag
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = drawing.content.asImageBitmap(),
                        contentDescription = "Drawing thumbnail for ${drawing.name}",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = drawing.name)
                        IconButton(onClick = { viewModel.deleteDrawing(drawing) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
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
fun AuthScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sign in to manage your drawings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            if (state.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSignIn,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Signing In..." else "Sign In")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onSignUp,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isLoading) "Creating..." else "Create Account")
            }
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
