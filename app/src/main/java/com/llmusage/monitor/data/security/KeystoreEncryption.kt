package com.llmusage.monitor.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Wraps Android Keystore AES/GCM/NoPadding so provider API keys can be stored
 * encrypted on disk. The key material is hardware-backed where the device
 * supports it. Decryption is only possible on the same device + app install.
 *
 * Encrypted payloads are written as two Base64-encoded strings (ciphertext +
 * IV) — Room stores them as plain TEXT columns.
 */
object KeystoreEncryption {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "llm_usage_monitor_api_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_BITS = 128

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()
        gen.init(spec)
        return gen.generateKey()
    }

    data class Encrypted(val cipherTextB64: String, val ivB64: String)

    fun encrypt(plaintext: String): Encrypted {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, getOrCreateKey()) }
        val bytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Encrypted(
            cipherTextB64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
            ivB64 = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        )
    }

    fun decrypt(cipherTextB64: String, ivB64: String): String {
        val iv = Base64.decode(ivB64, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        val bytes = Base64.decode(cipherTextB64, Base64.NO_WRAP)
        return String(cipher.doFinal(bytes), Charsets.UTF_8)
    }

    /** Best-effort wipe — call from "reset all data" / "uninstall on logout". */
    fun deleteKey() {
        runCatching {
            val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (ks.containsAlias(KEY_ALIAS)) ks.deleteEntry(KEY_ALIAS)
        }
    }
}
