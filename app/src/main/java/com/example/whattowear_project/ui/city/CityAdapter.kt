package com.example.whattowear_project.ui.city

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.whattowear_project.databinding.ItemCityBinding
import com.example.whattowear_project.weather.model.City

/** Списък с резултати от търсенето на град. */
class CityAdapter(
    private val onCityClick: (City) -> Unit,
) : ListAdapter<City, CityAdapter.CityViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder =
        CityViewHolder(ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) = holder.bind(getItem(position))

    inner class CityViewHolder(private val binding: ItemCityBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(city: City) {
            binding.textCityName.text = city.name
            binding.textCityDescription.text = city.description
            binding.textCityDescription.isVisible = city.description.isNotEmpty()
            binding.root.setOnClickListener { onCityClick(city) }
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<City>() {
            override fun areItemsTheSame(oldItem: City, newItem: City) =
                oldItem.latitude == newItem.latitude && oldItem.longitude == newItem.longitude

            override fun areContentsTheSame(oldItem: City, newItem: City) = oldItem == newItem
        }
    }
}
