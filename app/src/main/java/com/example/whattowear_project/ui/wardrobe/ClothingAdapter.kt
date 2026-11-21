package com.example.whattowear_project.ui.wardrobe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.whattowear_project.R
import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.databinding.ItemClothingBinding
import com.example.whattowear_project.ui.common.labelRes
import com.example.whattowear_project.ui.common.showClothingImage

/**
 * Адаптер за решетката с дрехи.
 * ListAdapter + DiffUtil: при промяна прерисува само картите, които наистина са се сменили.
 */
class ClothingAdapter(
    private val onItemClick: (ClothingItem) -> Unit,
) : ListAdapter<ClothingItem, ClothingAdapter.ClothingViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothingViewHolder {
        val binding = ItemClothingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ClothingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClothingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClothingViewHolder(
        private val binding: ItemClothingBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClothingItem) {
            val context = binding.root.context

            binding.textName.text = item.name
            // Подвидът е по-точен („Дънки“), ако няма - основният тип („Долнище“).
            binding.textType.setText(item.subtype?.labelRes ?: item.type.labelRes)
            binding.textTemp.text = context.getString(R.string.temp_range, item.minTemp, item.maxTemp)
            binding.badgeWaterproof.isVisible = item.waterproof

            // Снимка или цветен фон + икона на типа.
            showClothingImage(item, binding.photoContainer, binding.imageTypeIcon, binding.imagePhoto)

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ClothingItem>() {
            override fun areItemsTheSame(oldItem: ClothingItem, newItem: ClothingItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ClothingItem, newItem: ClothingItem) =
                oldItem == newItem
        }
    }
}
