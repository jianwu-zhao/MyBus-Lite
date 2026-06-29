package com.mygolbs.mybus

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mygolbs.mybus.databinding.ItemBusLineBinding
import com.mygolbs.mybus.model.BusLine

class LineAdapter(
    private val onClick: (BusLine) -> Unit
) : ListAdapter<BusLine, LineAdapter.ViewHolder>(DiffCallback) {

    /** 当前城市名（用于传递给详情页） */
    var currentCityName: String = "厦门"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBusLineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemBusLineBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(line: BusLine) {
            binding.lineName.text = line.lineName
            binding.lineRoute.text = "${line.fromName} → ${line.toName}"
            binding.lineTime.text = "${line.startTime} - ${line.endTime}"
            binding.linePrice.text = if (line.price.isNotBlank()) "票价: ${line.price}元" else "点击查看站点"
            binding.root.setOnClickListener { onClick(line) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<BusLine>() {
        override fun areItemsTheSame(old: BusLine, new: BusLine) = old.lineId == new.lineId
        override fun areContentsTheSame(old: BusLine, new: BusLine) = old == new
    }
}
