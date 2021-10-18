package com.example.backup

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESService {
    private var iv = byteArrayOf(
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16
    )

    fun encByKey(key: String, plainText: String): String {
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

        val randomKey = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(randomKey, 0)
    }

    fun decByKey(key: String, cryptogram: String): String{
        val secretKey = SecretKeySpec(key.toByteArray(), "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        val desText = cipher.doFinal(Base64.decode(cryptogram, 0))
        return String(desText)
    }
}