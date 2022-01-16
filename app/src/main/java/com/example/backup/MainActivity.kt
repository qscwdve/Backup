package com.example.backup

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var fileFindActivity : ActivityResultLauncher<Intent>
    lateinit var sendEmailActivity : ActivityResultLauncher<Intent>
    private val backupFileName = "backup.txt"
    private val key = "secretKey"
    companion object {
        const val REQUEST_ALL_PERMISSION = 1
        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
            //Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    @SuppressLint("SdCardPath")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setResultFindFile()
        setResultSendMail()

        // 파일 탐색기
        findViewById<Button>(R.id.main_file_find_btn).setOnClickListener {
            findFile()
        }
        // 이메일 보내기
        findViewById<Button>(R.id.main_send_email_btn).setOnClickListener {
            val content = findViewById<EditText>(R.id.main_content).text.toString()
            if(content != ""){
                sendMail(content)
            }
        }
        // 압축파일 만들기
        // 권한 확인 -> API 23 이상 가능
        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }
        //zipTest()
        ZIPManager().unzip("/data/data/com.example.backup/files/zipTest/ziptest.zip", "/data/data/com.example.backup/files/")
    }

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    @SuppressLint("SdCardPath")
    fun zipTest(){
        //내부저장 경로이다.
        val mOutputDir = "/data/data/com.example.backup/files"

        // 파일 저장
        val file: String = "file1"
        val file2: String = "file2"

        Log.d("fileZiP", "path : $mOutputDir")
        val data: String = "이것은 테스트입니다. 압축파일 테스트!"
        val data2 = "Hello, This is a test!! \n test is good!"
        var fileOutputStream: FileOutputStream
        try {
            fileOutputStream = openFileOutput(file, Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
            //fileOutputStream.close()

            fileOutputStream = openFileOutput(file2, Context.MODE_PRIVATE)
            fileOutputStream.write(data2.toByteArray())
            //fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val s = arrayOfNulls<String>(2)

        // Type the path of the files in here
        s[0] = "$mOutputDir/$file"
        s[1] = "$mOutputDir/$file2"

        // first parameter is d files second parameter is zip file name
        val zipManager = ZIPManager()

        //저장할 폴더를 만든다.
        val dir: File = File("$mOutputDir/zipTest")
        if (!dir.exists()) {
            dir.mkdirs()
        }

        //파일 압축
        zipManager.zip(s, "$mOutputDir/zipTest/ziptest.zip")
        //폴더 압축
        //zipManager.zipFolder(mOutputDir,"$mOutputDir/zipTest/ziptest.zip")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_ALL_PERMISSION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
                } else {
                    //requestPermissions(permissions, REQUEST_ALL_PERMISSION)
                    Toast.makeText(this, "Permissions must be granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 이메일로 백업 파일 보낸 뒤 작업
    private fun setResultSendMail(){
        sendEmailActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if(result.resultCode == RESULT_OK){
                    // 임시 파일 삭제
                    val sendFilePath = filesDir.canonicalPath + "/" + backupFileName
                    val file = File(sendFilePath)
                    file.delete()
                }
            }
    }

    // 파일 탐색기를 통해 불러온 파일 결과
    private fun setResultFindFile(){
        fileFindActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if(result.resultCode == RESULT_OK){
                    val fileUri : Uri? = result.data?.data
                    if(fileUri != null){
                        // 파일이 있을 경우 파일 내용 불러온다.
                        val content = readTextFile(fileUri)
                        // 파일 복호화
                        val desContent = AESService().decByKey(key, content)
                        // 파일 내용 뷰에 적용
                        findViewById<EditText>(R.id.main_content).setText(desContent)
                    }
                }
            }

    }

    // 파일 탐색기를 통해 파일 불러오기
    private fun findFile(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        fileFindActivity.launch(intent)
    }

    // 파일 읽기
    @Throws(IOException::class)
    fun readTextFile(uri: Uri) : String{
        val contentResolver = applicationContext.contentResolver
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    line += "\n"
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    // 이메일 보내기
    private fun sendMail(fileContent: String){
        try {
            // 내용 암호화
            val encFileContent = AESService().encByKey(key, fileContent)
            // 임시 파일 만들기
            val fileOutputStream: FileOutputStream  = openFileOutput(backupFileName, Context.MODE_PRIVATE)
            fileOutputStream.write(encFileContent.toByteArray())
            fileOutputStream.close()

            val sendFilePath = filesDir.canonicalPath + "/" + backupFileName
            val file = File(sendFilePath)

            val sendUri = FileProvider.getUriForFile(this, "com.example.backup.provider", file)
            /* 첨부파일 이메일 보내기 */
            val intent = Intent(Intent.ACTION_SEND).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                type = "plain/text"
                putExtra(Intent.EXTRA_SUBJECT, "Dream Tree Back file Test")
                putExtra(Intent.EXTRA_STREAM, sendUri)
            }
            sendEmailActivity.launch(intent)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}