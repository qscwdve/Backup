package com.example.backup

import android.app.Activity
import android.content.Context
import android.util.Log
import java.io.*

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
}