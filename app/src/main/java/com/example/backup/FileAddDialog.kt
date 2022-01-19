package com.example.backup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.backup.databinding.DialogFileAddBinding

class FileAddDialog(val mainActivity: MainActivity) : DialogFragment(){
    private lateinit var binding : DialogFileAddBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFileAddBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dialogFileAddInsert.setOnClickListener {
            mainActivity.fileAdd(
                FileFormat(
                    binding.dialogFileAddTitle.text.toString(),
                    binding.dialogFileAddContent.text.toString()
                )
            )
            dismiss()
        }
    }
}