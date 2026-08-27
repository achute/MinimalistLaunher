package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.data.LauncherTheme

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantDarkAccent,
    onPrimary = Color(0xFF14110A),
    primaryContainer = ElegantDarkSurfaceVariant,
    onPrimaryContainer = Color(0xFFF5F5F7),
    secondary = ElegantDarkSecondary,
    onSecondary = Color(0xFF14110A),
    background = ElegantDarkBackground,
    onBackground = ElegantDarkOnBackground,
    surface = ElegantDarkSurface,
    onSurface = ElegantDarkOnBackground,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = ElegantDarkOnSurfaceVariant,
    outline = ElegantDarkOutline
)

private val OledColorScheme = darkColorScheme(
    primary = OledAccent,
    onPrimary = Color.Black,
    primaryContainer = OledSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = OledOnSurfaceVariant,
    onSecondary = Color.Black,
    background = OledBackground,
    onBackground = OledOnBackground,
    surface = OledSurface,
    onSurface = OledOnBackground,
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = OledOnSurfaceVariant,
    outline = OledOutline
)

private val EInkColorScheme = lightColorScheme(
    primary = EInkAccent,
    onPrimary = Color.White,
    primaryContainer = EInkSurfaceVariant,
    onPrimaryContainer = EInkOnBackground,
    secondary = EInkOnSurfaceVariant,
    onSecondary = Color.White,
    background = EInkBackground,
    onBackground = EInkOnBackground,
    surface = EInkSurface,
    onSurface = EInkOnBackground,
    surfaceVariant = EInkSurfaceVariant,
    onSurfaceVariant = EInkOnSurfaceVariant,
    outline = EInkOutline
)

private val AmberColorScheme = darkColorScheme(
    primary = AmberAccent,
    onPrimary = Color.Black,
    primaryContainer = AmberSurfaceVariant,
    onPrimaryContainer = AmberOnBackground,
    secondary = AmberOnSurfaceVariant,
    onSecondary = Color.Black,
    background = AmberBackground,
    onBackground = AmberOnBackground,
    surface = AmberSurface,
    onSurface = AmberOnBackground,
    surfaceVariant = AmberSurfaceVariant,
    onSurfaceVariant = AmberOnSurfaceVariant,
    outline = AmberOutline
)

private val MatrixColorScheme = darkColorScheme(
    primary = MatrixAccent,
    onPrimary = Color.Black,
    primaryContainer = MatrixSurfaceVariant,
    onPrimaryContainer = MatrixOnBackground,
    secondary = MatrixOnSurfaceVariant,
    onSecondary = Color.Black,
    background = MatrixBackground,
    onBackground = MatrixOnBackground,
    surface = MatrixSurface,
    onSurface = MatrixOnBackground,
    surfaceVariant = MatrixSurfaceVariant,
    onSurfaceVariant = MatrixOnSurfaceVariant,
    outline = MatrixOutline
)

private val SlateColorScheme = darkColorScheme(
    primary = SlateAccent,
    onPrimary = Color.Black,
    primaryContainer = SlateSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = SlateOnSurfaceVariant,
    onSecondary = Color.Black,
    background = SlateBackground,
    onBackground = SlateOnBackground,
    surface = SlateSurface,
    onSurface = SlateOnBackground,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = SlateOnSurfaceVariant,
    outline = SlateOutline
)

private val CyanColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = Color.Black,
    primaryContainer = CyanSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = CyanOnSurfaceVariant,
    onSecondary = Color.Black,
    background = CyanBackground,
    onBackground = CyanOnBackground,
    surface = CyanSurface,
    onSurface = CyanOnBackground,
    surfaceVariant = CyanSurfaceVariant,
    onSurfaceVariant = CyanOnSurfaceVariant,
    outline = CyanOutline
)

@Composable
fun MinimalistLauncherTheme(
    theme: LauncherTheme = LauncherTheme.ELEGANT_DARK,
    fontFamily: FontFamily = FontFamily.Monospace,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        LauncherTheme.ELEGANT_DARK -> ElegantDarkColorScheme
        LauncherTheme.MONOCHROME_OLED -> OledColorScheme
        LauncherTheme.E_INK_PAPER -> EInkColorScheme
        LauncherTheme.AMBER_CRT -> AmberColorScheme
        LauncherTheme.RETRO_MATRIX -> MatrixColorScheme
        LauncherTheme.MINIMAL_SLATE -> SlateColorScheme
        LauncherTheme.CYBERPUNK_CYAN -> CyanColorScheme
    }

    val dynamicTypography = remember(fontFamily) {
        createLauncherTypography(fontFamily)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = dynamicTypography,
        content = content
    )
}
