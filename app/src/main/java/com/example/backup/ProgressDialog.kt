package com.example.backup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.backup.databinding.DialogProgressBinding

class ProgressDialog(val mainActivity: MainActivity) : DialogFragment(){
    lateinit var binding : DialogProgressBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogProgressBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 백업 파일 만들기 시작
        mainActivity.startBackUpFile(this)
    }

    fun setProgressBarGaze(progress: Int){
        val gazePercent = "${progress}%"
        binding.dialogProgressBar.progress = progress
        binding.dialogProgressPercent.text = gazePercent
        //Log.d("progress", "progress : $progress")
    }
}