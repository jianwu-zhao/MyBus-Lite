package com.mygolbs.mybus

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mygolbs.mybus.databinding.ItemStationBinding
import com.mygolbs.mybus.model.Station

class StationAdapter : ListAdapter<Station, StationAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ViewHolder(
        private val binding: ItemStationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(station: Station, position: Int) {
            binding.stationName.text = station.stationName
            binding.stationOrder.text = "${position + 1}"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Station>() {
        override fun areItemsTheSame(old: Station, new: Station) = old.stationId == new.stationId
        override fun areContentsTheSame(old: Station, new: Station) = old == new
    }
}
