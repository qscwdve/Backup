package com.example.backup

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.backup.databinding.ActivityMainBinding
import java.io.*

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    lateinit var fileFindActivity : ActivityResultLauncher<Intent>
    lateinit var sendEmailActivity : ActivityResultLauncher<Intent>
    private val backupFileName = "backup.zip"
    private val EncFileName = "backup.txt"
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setResultFindFile()
        setResultSendMail()

        // 파일 리스트
        binding.mainFileList.adapter = FileListAdapter(FileControl(this).getFileList(), this)
        binding.mainFileList.layoutManager = LinearLayoutManager(this)

        // 파일 추가
        binding.mainFileAdd.setOnClickListener {
            val fileAddDialog = FileAddDialog(this)
            fileAddDialog.show(supportFragmentManager, "add")
        }

        // 파일 삭제
        binding.mainFileDelete.setOnClickListener {
            FileControl(this).deleteTotalFile()
            (binding.mainFileList.adapter as FileListAdapter?)?.deleteTotalItem()
        }

        // 파일 탐색기
        binding.mainFileFindBtn.setOnClickListener {
            findFile()
        }
        // 이메일 보내기
        binding.mainSendEmailBtn.setOnClickListener {
            // 다이얼로그 띄우기
            val progressDialog = ProgressDialog(this)
            progressDialog.show(supportFragmentManager, "progress")

            //sendMail(content)
        }

        // 압축파일 만들기
        // 권한 확인 -> API 23 이상 가능
        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }

        //zipTest()
        //ZIPManager().unzip("/data/data/com.example.backup/files/zipTest/ziptest.zip", "/data/data/com.example.backup/files/")
    }

    // 내부파일 삭제하기
    fun innerDeleteFile(fileName: String){
        FileControl(this).deleteFile(fileName)
    }

    // 백업파일을 만든 뒤 메일로 보내기
    fun startBackUpFileSendMail(){
        sendMail()
    }

    // 백업 파일 만들기 진행사항 프로그래스바에 표시하기
    fun applyBackupProgress(progressDialog: ProgressDialog, num: Int){
        progressDialog.setProgressBarGaze(num)
    }

    // 백업 파일 만들기 시작하기
    @SuppressLint("SdCardPath")
    fun startBackUpFile(progressDialog: ProgressDialog){
        // 압축파일 만들기
        val basePath = "/data/data/com.example.backup/files/"
        val files = this.fileList()
        for(index in files.indices){
            files[index] = basePath + files[index]
        }
        ZIPManager().zip(files, basePath + backupFileName, progressDialog, this)

    }

    fun fileAdd(file: FileFormat){
        FileControl(this).addFile(file)
        (binding.mainFileList.adapter as FileListAdapter?)?.addItem(file)
    }

    fun showFileInfoDialog(file: FileFormat){
        val fileInfoDialog = FileInfoDialog(file)
        fileInfoDialog.show(supportFragmentManager, "info")
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

        //Log.d("fileZiP", "path : $mOutputDir")
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
        //zipManager.zip(s, "$mOutputDir/zipTest/ziptest.zip", this)
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
    @SuppressLint("SdCardPath")
    private fun setResultFindFile(){
        val basePath = "/data/data/com.example.backup/files/"
        fileFindActivity =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if(result.resultCode == RESULT_OK){
                    val fileUri : Uri? = result.data?.data
                    if(fileUri != null){
                        // 파일이 있을 경우 파일 내용 불러온다.
                        val content = readTextFile(fileUri)
                        // 파일 복호화
                        val desContent = AESService().decByKey(key, content)
                        // 내부 파일로 저장
                        FileControl(this).addFile(FileFormat(backupFileName, desContent))
                        // unZip
                        ZIPManager().unzip(basePath + backupFileName, basePath)
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
    private fun sendMail(){
        // 파일 내용 불러오기
        val fileContent = FileControl(this).getFileContent(backupFileName)
        // 백업 파일 삭제
        FileControl(this).deleteFile(backupFileName)
        try {
            // 내용 암호화
            val encFileContent = AESService().encByKey(key, fileContent)
            // 임시 파일 만들기
            val fileOutputStream: FileOutputStream  = openFileOutput(EncFileName, Context.MODE_PRIVATE)
            fileOutputStream.write(encFileContent.toByteArray())
            fileOutputStream.close()

            val sendFilePath = filesDir.canonicalPath + "/" + EncFileName
            val file = File(sendFilePath)

            val sendUri = FileProvider.getUriForFile(this, "com.example.backup.provider", file)
            /* 첨부파일 이메일 보내기 */
            val mail_intent = Intent(Intent.ACTION_SEND)
            mail_intent.type = "*/*"
            mail_intent.putExtra(Intent.EXTRA_SUBJECT, "백업파일") // 메일 제목
            mail_intent.putExtra(Intent.EXTRA_TEXT, "백업파일이 첨부파일로 전송됩니다.") // 메일 내용
            mail_intent.putExtra(Intent.EXTRA_STREAM, sendUri)

            val intent = Intent(Intent.ACTION_SEND).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                type = "plain/text"
                putExtra(Intent.EXTRA_SUBJECT, "Dream Tree Back file Test")
                putExtra(Intent.EXTRA_STREAM, sendUri)
            }
            sendEmailActivity.launch(mail_intent)
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