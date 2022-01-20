package com.example.backup

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
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
            val progressDialog = ProgressDialog(this, 1, backupFileName)
            progressDialog.show(supportFragmentManager, "progress")

        }

        // 압축파일 만들기
        // 권한 확인 -> API 23 이상 가능
        if (!hasPermissions(this, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, REQUEST_ALL_PERMISSION)
        }
    }

    // 내부파일 삭제하기
    fun innerDeleteFile(fileName: String){
        FileControl(this).deleteFile(fileName)
    }

    // 백업파일을 만든 뒤 메일로 보내기
    fun startBackUpFileSendMail(fileName: String){
        sendMail(fileName)
    }

    // 백업 파일로 복원하기
    @SuppressLint("SdCardPath")
    fun startBackupFileSave(dialog: ProgressDialog, saveFileName: String){
        val basePath = "/data/data/com.example.backup/files/"
        //Log.d("fileName", saveFileName)
        // unZip
        ZIPManager().unzip(basePath + saveFileName, basePath, dialog, this@MainActivity)
        // 내부저장소에 저장된 백업 파일 지우기
        FileControl(this).deleteFile(saveFileName)
        // 다시 파일 리스트 뿌려주기
        (binding.mainFileList.adapter as FileListAdapter?)?.setItemList(FileControl(this).getFileList())
    }

    // 백업 파일 만들기 진행사항 프로그래스바에 표시하기
    fun applyBackupProgress(progressDialog: ProgressDialog, num: Int){
        progressDialog.setProgressBarGaze(num)
    }

    // 백업 파일 만들기 시작하기
    @SuppressLint("SdCardPath")
    fun startBackUpFile(progressDialog: ProgressDialog, fileName: String){
        // 압축파일 만들기
        val basePath = "/data/data/com.example.backup/files/"
        val files = this.fileList()

        ZIPManager().zip(fileName, basePath, files, basePath + backupFileName, progressDialog, this)
        //Log.d("Compress", "start Backup file Create")
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
                // 백업 파일 삭제
                FileControl(this).deleteFile(backupFileName)
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
                        // 내부 파일로 저장
                        val saveFileName = FileControl(this).saveFile(basePath, fileUri)
                        if(saveFileName != null){
                            // 다이얼로그 띄우기
                            val progressDialog = ProgressDialog(this, 2, saveFileName)
                            progressDialog.show(supportFragmentManager, "backup")
                        }
                     }
                }
            }

    }
    // 백업과정 도중 강제 백업 과정취소
    fun stopBackup(dialog: ProgressDialog){
        ZIPManager().stopProgress()
    }

    // 파일 탐색기를 통해 파일 불러오기
    private fun findFile(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        fileFindActivity.launch(intent)
    }

    // 이메일 보내기
    private fun sendMail(fileName: String){
        try {

            val sendFilePath = filesDir.canonicalPath + "/" + fileName
            //Log.d("sendFile", "$sendFilePath")
            val file = File(sendFilePath)

            val sendUri = FileProvider.getUriForFile(this, "com.example.backup.provider", file)
            /* 첨부파일 이메일 보내기 */
            val mail_intent = Intent(Intent.ACTION_SEND)
            mail_intent.type = "*/*"
            mail_intent.putExtra(Intent.EXTRA_SUBJECT, "백업파일") // 메일 제목
            mail_intent.putExtra(Intent.EXTRA_TEXT, "백업파일이 첨부파일로 전송됩니다.") // 메일 내용
            mail_intent.putExtra(Intent.EXTRA_STREAM, sendUri)

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