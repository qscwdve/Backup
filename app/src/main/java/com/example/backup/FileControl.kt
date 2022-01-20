package com.example.backup

import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.*
import java.nio.channels.FileChannel

class FileControl(val context: Activity){

    fun addFile(fileData : FileFormat){
        //파일 내부 저장
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = context.openFileOutput(fileData.title, Context.MODE_PRIVATE)
            fileOutputStream.write(fileData.content.toByteArray())
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

    fun deleteTotalFile(){
        // 내부 저장된 파일 목록 가져오기
        val files: Array<String> = context.fileList()

        for (fileName in files){
            // 내부 저장된 파일 삭제
            context.deleteFile(fileName)
        }
    }

    fun deleteFile(filename: String){
        context.deleteFile(filename)
    }

    fun getFileList(): ArrayList<FileFormat>{
        val array = ArrayList<FileFormat>()

        // 내부 저장된 파일 목록 가져오기
        val files: Array<String> = context.fileList()

        for (fileName in files){
            //Log.d("fileList", f)
            array.add(FileFormat(fileName, getFileContent(fileName)))
        }
        return array
    }

    // 내부 저장된 파일 내용 가져오기
    fun getFileContent(filename: String): String{
        try {
            var fileInputStream: FileInputStream? = null

            if (context.openFileInput(filename) != null) {
                fileInputStream = context.openFileInput(filename)
                val inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
                val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
                val stringBuilder: StringBuilder = StringBuilder()
                var text: String? = null
                while ({ text = bufferedReader.readLine(); text }() != null) {
                    stringBuilder.append(text)
                }
                //Log.d("fileList", "content : ${stringBuilder.toString()}")
                return stringBuilder.toString() ?: ""
            }

        } catch (e: NumberFormatException) {

        }
        return ""
    }

    fun saveFile(basePath: String, uri: Uri): String?{
        var pfd: ParcelFileDescriptor? = null
        var fileInputStream: FileInputStream? = null
        val fileName = getFileName(uri)
        try {
            pfd = uri.let { context.applicationContext.contentResolver?.openFileDescriptor(it, "r") }
            fileInputStream = FileInputStream(pfd?.fileDescriptor)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        var newFile: File? = null
        if(fileName!=null) {
            newFile = File(basePath, fileName)
        }

        var inChannel: FileChannel? = null
        var outChannel: FileChannel? = null

        try {
            inChannel = fileInputStream?.channel
            outChannel = FileOutputStream(newFile).channel
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        try {
            inChannel?.transferTo(0, inChannel.size(), outChannel)
        } finally {
            inChannel?.close()
            outChannel?.close()
            fileInputStream?.close()
            pfd?.close()
        }
        return fileName
    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.applicationContext.contentResolver?.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    // 파일 읽기
    @Throws(IOException::class)
    fun readTextFile(uri: Uri) : String{
        val contentResolver = context.applicationContext.contentResolver
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
}