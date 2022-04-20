package com.example.share.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.share.R
import com.example.share.databinding.ItemFileBinding

class FilesAdapter(private val filesList: ArrayList<String>): RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    class FileViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding = ItemFileBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        return FileViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_file, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
//        holder.binding.textView.text = filesList[position]
    }

    override fun getItemCount(): Int {
        return filesList.size
    }
}