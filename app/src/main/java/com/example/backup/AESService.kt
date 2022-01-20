package com.example.backup

import android.app.Activity
import android.util.Base64
import java.lang.Exception
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESService {
    private val key = "secretKey"
    private var iv = byteArrayOf(
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16
    )

    fun encByKey(key: String, plainText: String): String {
        val secretKey = SecretKeySpec(createHashKey(key).toByteArray(), "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

        val randomKey = cipher.doFinal(plainText.toByteArray())
        return Base64.encodeToString(randomKey, 0)
    }

    fun decByKey(key: String, cryptogram: String): String{
        val secretKey = SecretKeySpec(createHashKey(key).toByteArray(), "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        val desText = cipher.doFinal(Base64.decode(cryptogram, 0))
        return String(desText)
    }

    private fun createHashKey(key: String) : String {
        try {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(key.toByte())

            return md.digest().toString().substring(0..15)
        } catch (e : Exception){ }
        return "secretKey1234567"
    }

    fun encFile(context: Activity, fileName: String){
        // 파일 내용 가져오기
        val fileData = FileControl(context).getFileContent(fileName)
        // 파일 암호화하기
        val endFileData = encByKey(key, fileData)
        // 파일 암호화한 내용으로 덮어쓰기
        FileControl(context).addFile(FileFormat(fileName, endFileData))
    }

    fun decFile(context: Activity, fileName: String){
        // 파일 내용 가져오기
        val encFileData = FileControl(context).getFileContent(fileName)
        // 파일 복호화하기
        val fileData = decByKey(key, encFileData)
        // 복호화한 파일 내용 덮어쓰기
        FileControl(context).addFile(FileFormat(fileName, fileData))
    }
}