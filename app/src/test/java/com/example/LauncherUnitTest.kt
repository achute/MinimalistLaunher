package com.example

import com.example.model.AppInfoItem
import com.example.model.BottomSlot
import com.example.model.FocusProfile
import com.example.model.TaskItem
import com.example.util.BatteryStatus
import com.example.util.CryptoUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LauncherUnitTest {

    @Test
    fun testCryptoUtilEncryptionDecryption() {
        val originalText = "com.instagram.android"
        val encrypted = CryptoUtil.encrypt(originalText)
        assertNotNull(encrypted)
        assertTrue(encrypted.startsWith("enc_"))

        val decrypted = CryptoUtil.decrypt(encrypted)
        assertEquals(originalText, decrypted)
    }

    @Test
    fun testCryptoUtilFallbackForPlaintext() {
        val plaintext = "com.spotify.music"
        val decrypted = CryptoUtil.decrypt(plaintext)
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun testAppInfoItemDisplayLabel() {
        val item1 = AppInfoItem(packageName = "com.android.chrome", label = "Chrome")
        assertEquals("Chrome", item1.displayLabel)

        val item2 = AppInfoItem(packageName = "com.android.chrome", label = "Chrome", customLabel = "Web Browser")
        assertEquals("Web Browser", item2.displayLabel)
    }

    @Test
    fun testFocusProfilePackages() {
        val profile = FocusProfile(
            id = 1,
            name = "Work",
            appPackage1 = "com.google.android.gm",
            appPackage2 = "com.google.android.keep",
            appPackage3 = "",
            appPackage4 = "",
            appPackage5 = ""
        )
        val packages = profile.getPackages()
        assertEquals(2, packages.size)
        assertEquals("com.google.android.gm", packages[0])
        assertEquals("com.google.android.keep", packages[1])
    }

    @Test
    fun testBatteryStatusDefaults() {
        val status = BatteryStatus()
        assertEquals(100, status.level)
        assertFalse(status.isCharging)
        assertEquals("Good", status.health)
    }

    @Test
    fun testTaskItem() {
        val task = TaskItem(title = "Read a book", isDone = false)
        assertEquals("Read a book", task.title)
        assertFalse(task.isDone)
    }

    @Test
    fun testLauncherSettingsGestureDefaults() {
        val settings = com.example.data.LauncherSettings()
        assertEquals("browser", settings.swipeRightAction)
        assertEquals("utility", settings.swipeLeftAction)
    }
}
