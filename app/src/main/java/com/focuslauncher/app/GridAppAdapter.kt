package com.focuslauncher.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.focuslauncher.app.databinding.ItemAppGridBinding

class GridAppAdapter(
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<GridAppAdapter.VH>() {

    private var items: List<AppInfo> = emptyList()

    fun submit(list: List<AppInfo>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAppGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(private val binding: ItemAppGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(app: AppInfo) {
            binding.icon.setImageDrawable(app.icon)
            binding.label.text = app.label
            binding.root.setOnClickListener { onClick(app) }
        }
    }
}
