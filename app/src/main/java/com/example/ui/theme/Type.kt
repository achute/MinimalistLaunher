package com.example.ui.theme

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.ClockFont
import com.example.util.FontManager

fun getFontFamily(fontType: ClockFont): FontFamily {
    return when (fontType) {
        ClockFont.RETRO_MONO -> FontFamily.Monospace
        ClockFont.CLEAN_SANS -> FontFamily.SansSerif
        ClockFont.ELEGANT_SERIF -> FontFamily.Serif
        ClockFont.DIGITAL_MATRIX -> FontFamily.Monospace
        ClockFont.TERMINAL_VT100 -> FontFamily.Monospace
        ClockFont.RETRO_TYPEWRITER -> FontFamily.Serif
        ClockFont.CUSTOM_FILE -> FontFamily.Monospace
    }
}

fun getFontFamilyWithContext(context: Context, fontType: ClockFont): FontFamily {
    return FontManager.resolveFontFamily(context, fontType)
}

fun createLauncherTypography(fontFamily: FontFamily): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 52.sp,
            lineHeight = 56.sp,
            letterSpacing = (-1).sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.5.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 1.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp
        )
    )
}

val Typography = createLauncherTypography(FontFamily.Monospace)
