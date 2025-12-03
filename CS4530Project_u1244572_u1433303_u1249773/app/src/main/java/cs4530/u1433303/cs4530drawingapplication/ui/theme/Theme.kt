package cs4530.u1433303.cs4530drawingapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = IndigoSoft,
    onPrimary = Midnight,
    secondary = TealBright,
    onSecondary = Midnight,
    tertiary = AmberBright,
    background = Midnight,
    surface = DeepSurface,
    onBackground = OnDark,
    onSurface = OnDark,
    surfaceVariant = Graphite,
    onSurfaceVariant = OnDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    secondary = TealAccent,
    onSecondary = Color.White,
    tertiary = AmberAccent,
    background = Cloud,
    surface = Color.White,
    onBackground = OnLight,
    onSurface = OnLight,
    surfaceVariant = Mist,
    onSurfaceVariant = OnLight,
    outline = OutlineLight
)

@Composable
fun CS4530DrawingApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
