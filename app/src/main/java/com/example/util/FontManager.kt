package com.example.util

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.data.ClockFont
import java.io.File
import java.io.FileOutputStream

object FontManager {
    private const val TAG = "FontManager"
    private const val FONTS_DIR = "custom_fonts"
    const val DEFAULT_CUSTOM_FONT_NAME = "custom_font.ttf"

    private var cachedCustomTypeface: Typeface? = null
    private var cachedCustomFontFamily: FontFamily? = null
    private var lastLoadedFontFile: String? = null

    /**
     * Resolves the Compose FontFamily for the given ClockFont selection.
     * If CUSTOM_FILE is selected, loads the typeface from local app storage.
     */
    fun resolveFontFamily(context: Context, clockFont: ClockFont): FontFamily {
        return when (clockFont) {
            ClockFont.RETRO_MONO -> FontFamily.Monospace
            ClockFont.CLEAN_SANS -> FontFamily.SansSerif
            ClockFont.ELEGANT_SERIF -> FontFamily.Serif
            ClockFont.DIGITAL_MATRIX -> FontFamily.Monospace
            ClockFont.TERMINAL_VT100 -> FontFamily.Monospace
            ClockFont.RETRO_TYPEWRITER -> FontFamily.Serif
            ClockFont.CUSTOM_FILE -> {
                getCustomFontFamily(context) ?: FontFamily.Monospace
            }
        }
    }

    /**
     * Retrieves the custom font family if a valid TTF/OTF file is saved in internal storage.
     */
    fun getCustomFontFamily(context: Context): FontFamily? {
        val fontFile = getCustomFontFile(context)
        if (!fontFile.exists() || fontFile.length() == 0L) {
            return null
        }

        if (cachedCustomFontFamily != null && lastLoadedFontFile == fontFile.absolutePath) {
            return cachedCustomFontFamily
        }

        return try {
            val typeface = Typeface.createFromFile(fontFile)
            if (typeface != null) {
                cachedCustomTypeface = typeface
                val fontFamily = FontFamily(typeface)
                cachedCustomFontFamily = fontFamily
                lastLoadedFontFile = fontFile.absolutePath
                fontFamily
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading custom font file: ${e.message}")
            null
        }
    }

    fun getCustomFontFile(context: Context): File {
        val dir = File(context.filesDir, FONTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, DEFAULT_CUSTOM_FONT_NAME)
    }

    fun hasCustomFont(context: Context): Boolean {
        val fontFile = getCustomFontFile(context)
        return fontFile.exists() && fontFile.length() > 0L
    }

    /**
     * Imports a user-selected TTF or OTF font file from a SAF content URI,
     * writes it to app internal storage, and validates that it can be parsed as a Typeface.
     * Returns true if import was successful.
     */
    fun importFontFromUri(context: Context, uri: Uri): Result<String> {
        return try {
            val targetFile = getCustomFontFile(context)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return Result.failure(Exception("Unable to open font file from storage"))

            // Validate that Android can parse it as a Typeface
            val testTypeface = Typeface.createFromFile(targetFile)
            if (testTypeface == null) {
                targetFile.delete()
                return Result.failure(Exception("File is not a valid TTF or OTF typeface"))
            }

            // Invalidate cache
            cachedCustomTypeface = testTypeface
            cachedCustomFontFamily = FontFamily(testTypeface)
            lastLoadedFontFile = targetFile.absolutePath

            // Extract display name
            val displayName = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
                ?: "Custom Font (TTF/OTF)"

            Result.success(displayName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import font: ${e.message}")
            Result.failure(e)
        }
    }

    fun clearCustomFont(context: Context) {
        try {
            val file = getCustomFontFile(context)
            if (file.exists()) {
                file.delete()
            }
            cachedCustomTypeface = null
            cachedCustomFontFamily = null
            lastLoadedFontFile = null
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing custom font: ${e.message}")
        }
    }
}
