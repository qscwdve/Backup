package com.example.backup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val key = "secretKey1234567"
        val plaintext = "안뇽안녕"

        val cryptogram = AESService().encByKey(key, plaintext)
        val desText = AESService().decByKey(key, cryptogram)

        findViewById<TextView>(R.id.main_text).text = desText
        Toast.makeText(this, "문장 : $desText", Toast.LENGTH_SHORT).show()
    }
}