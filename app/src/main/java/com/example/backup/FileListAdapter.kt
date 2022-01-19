package com.example.backup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FileListAdapter(val fileList: ArrayList<FileFormat>, val mainActivity: MainActivity) : RecyclerView.Adapter<FileListAdapter.FileListViewHolder>(){
    class FileListViewHolder(itemView: View, val mainActivity: MainActivity) : RecyclerView.ViewHolder(itemView) {
        val parent = itemView.findViewById<RelativeLayout>(R.id.item_file_parent)
        val title = itemView.findViewById<TextView>(R.id.item_file_title)
        val content = itemView.findViewById<TextView>(R.id.item_file_content)

        fun bind(file: FileFormat){
            title.text = file.title
            content.text = file.content

            parent.setOnClickListener {
                mainActivity.showFileInfoDialog(file)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file_list, parent, false)
        return FileListViewHolder(view, mainActivity)
    }

    override fun onBindViewHolder(holder: FileListViewHolder, position: Int) {
        holder.bind(fileList[position])
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    fun addItem(file : FileFormat){
        fileList.add(file)
        notifyItemChanged(fileList.lastIndex)
    }

    fun deleteTotalItem(){
        fileList.clear()
        notifyDataSetChanged()
    }
}