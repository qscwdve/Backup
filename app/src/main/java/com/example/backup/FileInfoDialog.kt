package com.example.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.backup.databinding.DialogFileInfoBinding

class FileInfoDialog(val file: FileFormat) : DialogFragment(){
    private lateinit var binding : DialogFileInfoBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFileInfoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dialogFileInfoTitle.text = file.title
        binding.dialogFileInfoContent.setText(file.content)
        binding.dialogFileInfoCancel.setOnClickListener { dismiss() }
    }
}