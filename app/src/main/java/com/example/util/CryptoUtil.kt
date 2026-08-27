package com.example.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "MinimalistVaultKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val PREFIX = "enc_gcm:"
    private const val GCM_TAG_LENGTH = 128

    private var inMemoryKey: SecretKey? = null

    private fun getOrCreateKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
            val entry = keyStore.getEntry(KEY_ALIAS, null)
            if (entry is KeyStore.SecretKeyEntry) {
                entry.secretKey
            } else {
                getFallbackKey()
            }
        } catch (e: Exception) {
            getFallbackKey()
        }
    }

    private fun getFallbackKey(): SecretKey {
        if (inMemoryKey == null) {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            inMemoryKey = keyGen.generateKey()
        }
        return inMemoryKey!!
    }

    fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return plainText
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateKey()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherBase64 = Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
            "$PREFIX$ivBase64:$cipherBase64"
        } catch (e: Exception) {
            // Fallback obfuscation if hardware cipher fails
            val fallbackBase64 = Base64.encodeToString(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            "enc_b64:$fallbackBase64"
        }
    }

    fun decrypt(cipherText: String): String {
        if (cipherText.isBlank()) return cipherText
        if (!cipherText.startsWith("enc_")) {
            return cipherText // plaintext fallback
        }

        return try {
            if (cipherText.startsWith(PREFIX)) {
                val payload = cipherText.removePrefix(PREFIX)
                val parts = payload.split(":", limit = 2)
                if (parts.size == 2) {
                    val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                    val cipherBytes = Base64.decode(parts[1], Base64.NO_WRAP)
                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
                    val secretKey = getOrCreateKey()
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                    val plainBytes = cipher.doFinal(cipherBytes)
                    String(plainBytes, Charsets.UTF_8)
                } else {
                    cipherText
                }
            } else if (cipherText.startsWith("enc_b64:")) {
                val payload = cipherText.removePrefix("enc_b64:")
                val bytes = Base64.decode(payload, Base64.NO_WRAP)
                String(bytes, Charsets.UTF_8)
            } else {
                cipherText
            }
        } catch (e: Exception) {
            cipherText
        }
    }
}
