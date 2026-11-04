package com.focuslauncher.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.focuslauncher.app.databinding.ItemAppCheckBinding

class CheckAppAdapter(
    private val selected: MutableSet<String>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<CheckAppAdapter.VH>() {

    private var items: List<AppInfo> = emptyList()

    fun submit(list: List<AppInfo>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAppCheckBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(private val binding: ItemAppCheckBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(app: AppInfo) {
            binding.icon.setImageDrawable(app.icon)
            binding.label.text = app.label
            binding.check.setOnCheckedChangeListener(null)
            binding.check.isChecked = selected.contains(app.packageName)
            val toggle = {
                if (selected.contains(app.packageName)) selected.remove(app.packageName)
                else selected.add(app.packageName)
                binding.check.isChecked = selected.contains(app.packageName)
                onSelectionChanged()
            }
            binding.root.setOnClickListener { toggle() }
            binding.check.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selected.add(app.packageName) else selected.remove(app.packageName)
                onSelectionChanged()
            }
        }
    }
}
