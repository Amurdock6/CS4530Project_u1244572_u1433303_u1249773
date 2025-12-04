package cs4530.u1433303.cs4530drawingapplication

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import cs4530.u1433303.cs4530drawingapplication.data.CloudSyncRepository
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
        val cloudSyncRepository = CloudSyncRepository.getInstance()

        val viewModelFactory = ViewModelFactory(drawingRepository, cloudSyncRepository)

        val mainViewModel: MainViewModel by viewModels { viewModelFactory }
        val drawingViewModel: DrawingViewModel by viewModels { viewModelFactory }
        val cloudSyncViewModel: CloudSyncViewModel by viewModels { viewModelFactory }
        val authViewModel: AuthViewModel by viewModels()

        enableEdgeToEdge()
        setContent {
            CS4530DrawingApplicationTheme {
                App(
                    mainViewModel = mainViewModel,
                    drawingViewModel = drawingViewModel,
                    authViewModel = authViewModel,
                    cloudSyncViewModel = cloudSyncViewModel
                )
            }
        }
    }
}

// put the application in here basically
@Composable
fun App(
    mainViewModel: MainViewModel,
    drawingViewModel: DrawingViewModel,
    authViewModel: AuthViewModel,
    cloudSyncViewModel: CloudSyncViewModel
) {
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
                cloudViewModel = cloudSyncViewModel,
                userEmail = authState.user?.email ?: "Unknown user",
                userId = authState.user?.uid,
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
                },
                onImportToCanvas = { bitmap ->
                    drawingViewModel.loadImportedBitmap(bitmap)
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
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.background
        )
    )
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            DrawingAppScreen(
                viewModel,
                onBack = onBack
            )
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    cloudViewModel: CloudSyncViewModel,
    userEmail: String,
    userId: String?,
    onSignOut: () -> Unit,
    onNewDrawing: () -> Unit,
    onOpenDrawing: (DrawingEntity) -> Unit,
    onImportToCanvas: (Bitmap) -> Unit
) {
    val drawings by viewModel.drawings.collectAsState()
    val cloudState by cloudViewModel.uiState.collectAsState()

    var shareTarget by remember { mutableStateOf<DrawingEntity?>(null) }
    var shareEmail by remember { mutableStateOf("") }
    val formatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val importToCanvas: (String) -> Unit = { imageUrl ->
        cloudViewModel.importDrawing(imageUrl) { bmp ->
            onImportToCanvas(bmp)
        }
    }

    LaunchedEffect(userId, userEmail) {
        if (!userId.isNullOrBlank()) {
            cloudViewModel.refreshUserData(userId, userEmail)
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f),
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Sketchbook",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = userEmail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            }
                            OutlinedButton(onClick = onSignOut) {
                                Text("Sign out")
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                }

                item {
                    Button(
                        onClick = onNewDrawing,
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Start a new drawing")
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            enabled = !userId.isNullOrBlank() && drawings.isNotEmpty(),
                            onClick = {
                                val latest = drawings.maxByOrNull { it.createdAt }
                                if (latest != null) {
                                    cloudViewModel.uploadDrawing(latest.name, latest.content) {
                                        cloudViewModel.refreshUserData(userId ?: "", userEmail)
                                    }
                                } else {
                                    cloudViewModel.clearMessage()
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Backup latest to cloud") }
                        OutlinedButton(
                            enabled = !userId.isNullOrBlank(),
                            onClick = {
                                cloudViewModel.refreshUserData(userId ?: "", userEmail)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Refresh cloud") }
                    }
                }

                item {
                    if (cloudState.isBusy) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                    cloudState.message?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 4.dp,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "Cloud ready",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Use Share via email or Backup to cloud on each drawing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    if (!userId.isNullOrBlank()) {
                                        cloudViewModel.refreshUserData(userId, userEmail)
                                    }
                                },
                                enabled = !userId.isNullOrBlank(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (userId.isNullOrBlank()) "Sign in first" else "Refresh")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Local drawings",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (drawings.isEmpty()) {
                    item { EmptyState("No drawings yet", "Start a new canvas and see it appear here.") }
                } else {
                    items(drawings) { drawing ->
                        val isBackedUp = cloudState.userDrawings.any { it.title == drawing.name }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenDrawing(drawing) }
                                .testTag(drawing.name),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Image(
                                    bitmap = drawing.content.asImageBitmap(),
                                    contentDescription = "Drawing thumbnail for ${drawing.name}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .background(
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                    .padding(6.dp)
                                )
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                ) {
                                    Text(
                                        text = drawing.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    if (isBackedUp) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.CloudDone,
                                                contentDescription = "Backed up",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = "Already in cloud",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Tap to open",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { shareTarget = drawing },
                                            enabled = !userId.isNullOrBlank(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) { Text("Email share") }
                                        Button(
                                            onClick = {
                                                if (!userId.isNullOrBlank()) {
                                                    cloudViewModel.uploadDrawing(
                                                        drawing.name,
                                                        drawing.content
                                                    ) {
                                                        cloudViewModel.refreshUserData(userId, userEmail)
                                                    }
                                                }
                                            },
                                            enabled = !userId.isNullOrBlank() && !isBackedUp,
                                            shape = RoundedCornerShape(10.dp)
                                        ) { Text("Backup to cloud") }
                                        IconButton(onClick = { viewModel.deleteDrawing(drawing) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "My cloud images",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "These live in Firebase Storage. Tap Refresh above after uploading, or import any backup into the canvas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (cloudState.userDrawings.isEmpty()) {
                    item { EmptyState("No cloud items yet", "Back up a drawing to access it anywhere.") }
                } else {
                    items(cloudState.userDrawings, key = { it.id }) { remote ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Saved to cloud",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = remote.imageUrl,
                                        contentDescription = remote.title,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(remote.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            formatter.format(remote.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { importToCanvas(remote.imageUrl) }) {
                                        Text("Import to canvas")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Shared by you",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Drawings you've shared. Unshare to remove them from recipients, or import them back into your canvas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (cloudState.sharedByMe.isEmpty()) {
                    item { EmptyState("Nothing shared yet", "Share a drawing by email to populate this list.") }
                } else {
                    items(cloudState.sharedByMe, key = { it.id }) { shared ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = shared.imageUrl,
                                        contentDescription = shared.title,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    )
                                    Column(Modifier.weight(1f)) {

                                        Text(
                                            text = "Shared",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Text(shared.title, style = MaterialTheme.typography.titleMedium)
                                            Spacer(Modifier.width(8.dp))


                                        Text(
                                            "To ${shared.receiverEmail}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            formatter.format(shared.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { importToCanvas(shared.imageUrl) }) {
                                        Text("Import to canvas")
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    TextButton(onClick = { cloudViewModel.unshareDrawing(shared.id) }) {
                                        Text("Unshare")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Shared with you",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Anything sent to ${userEmail.ifBlank { "your email" }} appears here. Use Remove to unshare from your view.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (cloudState.sharedWithMe.isEmpty()) {
                    item { EmptyState("Nothing shared yet", "Shared drawings sent to ${userEmail.ifBlank { "your email" }} will appear here.") }
                } else {
                    items(cloudState.sharedWithMe, key = { it.id }) { shared ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = shared.imageUrl,
                                        contentDescription = shared.title,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    )
                                    Column(Modifier.weight(1f)) {

                                        Text(
                                            text = "Shared with you",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Text(shared.title, style = MaterialTheme.typography.titleMedium)

                                        Spacer(Modifier.width(8.dp))



                                        Text(
                                            "From: ${shared.senderEmail}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            formatter.format(shared.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { importToCanvas(shared.imageUrl) }) {
                                        Text("Import to canvas")
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    TextButton(onClick = { cloudViewModel.unshareDrawing(shared.id) }) {
                                        Text("Remove")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = onNewDrawing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Start a new drawing")
                    }
                }
            }
        }
    }

    shareTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { shareTarget = null; shareEmail = "" },
            confirmButton = {
                TextButton(onClick = {
                    if (shareEmail.isNotBlank()) {
                        cloudViewModel.shareDrawing(target.name, target.content, shareEmail.trim()) {
                            if (!userId.isNullOrBlank()) {
                                cloudViewModel.refreshUserData(userId, userEmail)
                            }
                        }
                    }
                    shareTarget = null
                    shareEmail = ""
                }) { Text("Share") }
            },
            dismissButton = {
                TextButton(onClick = { shareTarget = null; shareEmail = "" }) {
                    Text("Cancel")
                }
            },
            title = { Text("Share drawing") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Send '${target.name}' to another user. They must sign in with this email to see it.")
                    OutlinedTextField(
                        value = shareEmail,
                        onValueChange = { shareEmail = it },
                        label = { Text("Recipient email") },
                        singleLine = true
                    )
                    Text(
                        text = "Tip: you can unshare later from the Shared with you section.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 12.dp,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "Style Streak",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sign in to continue sketching on any device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.error != null) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp)
                    )
                }

                Button(
                    onClick = onSignIn,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (state.isLoading) "Signing in..." else "Sign in")
                }
                OutlinedButton(
                    onClick = onSignUp,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (state.isLoading) "Creating..." else "Create account")
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_screen_image),
                contentDescription = "Splash logo image"
            )
            Text(
                text = "Style Streak",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Sketch fast. Save often. Share instantly.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}
