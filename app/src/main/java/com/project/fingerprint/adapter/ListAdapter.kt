package com.project.fingerprint.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.fingerprint.databinding.ItemLayoutBinding

/**
 * List items view and functionality
 * */

class ListAdapter : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    private var list = ArrayList<String>()

    inner class ViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.tv1.text = list[position]
    }

    override fun getItemCount() = list.size

    fun setData(list: ArrayList<String>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
}