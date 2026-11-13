package progamerbattle.com.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// High-Contrast Light Theme Surfaces & Accents
val HighContrastLightBackground = Color(0xFFF1F5F9)
val HighContrastLightSurface = Color(0xFFFFFFFF)
val HighContrastLightCard = Color(0xFFE2E8F0)
val HighContrastLightCardElevated = Color(0xFFF8FAFC)
val HighContrastLightBorder = Color(0xFFCBD5E1)
val HighContrastLightTextPrimary = Color(0xFF0F172A)
val HighContrastLightTextSecondary = Color(0xFF475569)
val HighContrastLightTextMuted = Color(0xFF64748B)

// Semantic Cyber Surface Tokens that adapt dynamically to Dark/Light mode
data class CyberSurfaces(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardElevated: Color,
    val border: Color,
    val borderHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val drawerGlassGradient: List<Color>,
    val drawerBorderGradient: List<Color>,
    val dockGradient: List<Color>,
    val walletCardGradient: List<Color>
)

val DarkCyberSurfaces = CyberSurfaces(
    isDark = true,
    background = CarbonBackground,
    surface = CarbonSurface,
    card = CarbonCard,
    cardElevated = CarbonCardElevated,
    border = CarbonBorder,
    borderHighlight = Color(0xFF00F0FF).copy(alpha = 0.5f),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    drawerGlassGradient = listOf(
        Color(0xFA0B101D),
        Color(0xF5070A14),
        Color(0xFC05070E)
    ),
    drawerBorderGradient = listOf(
        SpecularHighlightBright.copy(alpha = 0.45f),
        NeonCyan.copy(alpha = 0.35f),
        CarbonBorder.copy(alpha = 0.4f),
        NeonMagenta.copy(alpha = 0.3f)
    ),
    dockGradient = listOf(CarbonCardElevated, CarbonSurface),
    walletCardGradient = listOf(
        Color(0xFF141A29),
        Color(0xFF0F1522),
        Color(0xFF182236)
    )
)

val LightCyberSurfaces = CyberSurfaces(
    isDark = false,
    background = HighContrastLightBackground,
    surface = HighContrastLightSurface,
    card = HighContrastLightCard,
    cardElevated = HighContrastLightCardElevated,
    border = HighContrastLightBorder,
    borderHighlight = Color(0xFF009EB3).copy(alpha = 0.6f),
    textPrimary = HighContrastLightTextPrimary,
    textSecondary = HighContrastLightTextSecondary,
    textMuted = HighContrastLightTextMuted,
    drawerGlassGradient = listOf(
        Color(0xFAF8FAFC),
        Color(0xF5EDF2F7),
        Color(0xFCFFFFFF)
    ),
    drawerBorderGradient = listOf(
        Color(0x99FFFFFF),
        Color(0xFF009EB3).copy(alpha = 0.5f),
        HighContrastLightBorder,
        Color(0xFFD8006C).copy(alpha = 0.4f)
    ),
    dockGradient = listOf(HighContrastLightSurface, HighContrastLightCard),
    walletCardGradient = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF1F5F9),
        Color(0xFFE2E8F0)
    )
)

val LocalCyberSurfaces = compositionLocalOf { DarkCyberSurfaces }

private val EsportsDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CarbonBackground,
    primaryContainer = NeonCyanDark,
    onPrimaryContainer = NeonCyanLight,
    secondary = NeonMagenta,
    onSecondary = CarbonBackground,
    secondaryContainer = NeonMagentaDark,
    onSecondaryContainer = NeonMagentaLight,
    tertiary = ElectricPurple,
    onTertiary = TextPrimary,
    tertiaryContainer = ElectricPurpleDark,
    onTertiaryContainer = ElectricPurpleLight,
    background = CarbonBackground,
    onBackground = TextPrimary,
    surface = CarbonSurface,
    onSurface = TextPrimary,
    surfaceVariant = CarbonCard,
    onSurfaceVariant = TextSecondary,
    outline = CarbonBorder,
    error = DangerRed
)

private val EsportsLightColorScheme = lightColorScheme(
    primary = Color(0xFF009EB3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F7FA),
    onPrimaryContainer = Color(0xFF004953),
    secondary = Color(0xFFD8006C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE4E6),
    onSecondaryContainer = Color(0xFF5A002C),
    tertiary = Color(0xFF7C00CC),
    onTertiary = Color.White,
    background = HighContrastLightBackground,
    onBackground = HighContrastLightTextPrimary,
    surface = HighContrastLightSurface,
    onSurface = HighContrastLightTextPrimary,
    surfaceVariant = HighContrastLightCard,
    onSurfaceVariant = HighContrastLightTextSecondary,
    outline = HighContrastLightBorder,
    error = DangerRed
)

class ThemeController(initialDark: Boolean = true) {
    var isDarkTheme by mutableStateOf(initialDark)
        private set

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }

    fun setDark(dark: Boolean) {
        isDarkTheme = dark
    }
}

val LocalThemeController = compositionLocalOf { ThemeController(true) }

@Composable
fun ProGamerBattleTheme(
    themeController: ThemeController = remember { ThemeController(true) },
    content: @Composable () -> Unit
) {
    val isDark = themeController.isDarkTheme
    val colorScheme = if (isDark) EsportsDarkColorScheme else EsportsLightColorScheme
    val cyberSurfaces = if (isDark) DarkCyberSurfaces else LightCyberSurfaces
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val bg = (if (isDark) CarbonBackground else HighContrastLightBackground).toArgb()
                window.statusBarColor = bg
                window.navigationBarColor = bg
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalThemeController provides themeController,
        LocalCyberSurfaces provides cyberSurfaces
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ProGamerBattleTheme(content = content)
}
