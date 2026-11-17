package com.example.whattowear_project.ui.wardrobe

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.whattowear_project.R
import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.data.entity.ClothingSubtype
import com.example.whattowear_project.data.entity.ClothingType
import com.example.whattowear_project.databinding.FragmentWardrobeBinding
import com.example.whattowear_project.ui.common.labelRes
import com.example.whattowear_project.viewmodel.WardrobeViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Гардероб: всички дрехи като решетка от карти, с филтър по тип и подвид.
 * Фрагментът само ПОКАЗВА - данните и филтрите са във WardrobeViewModel.
 */
@AndroidEntryPoint
class WardrobeFragment : Fragment() {

    private var _binding: FragmentWardrobeBinding? = null
    private val binding get() = _binding!!

    // Hilt създава ViewModel-а и му подава Repository-то.
    private val viewModel: WardrobeViewModel by viewModels()

    /** Кой чип на кой основен тип отговаря. */
    private val chipToType = mapOf(
        R.id.chip_all to null,
        R.id.chip_top to ClothingType.TOP,
        R.id.chip_bottom to ClothingType.BOTTOM,
        R.id.chip_jacket to ClothingType.JACKET,
        R.id.chip_shoes to ClothingType.SHOES,
        R.id.chip_accessory to ClothingType.ACCESSORY,
    )

    /** За кой тип е построен в момента редът с подвидове (null = скрит). */
    private var subtypeRowType: ClothingType? = null

    /** true докато пренареждаме чиповете от кода - тогава не пращаме събития към ViewModel-а. */
    private var isUpdatingChips = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWardrobeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Адаптерът е локална променлива, а не поле - така не задържа стария изглед (memory leak).
        val adapter = ClothingAdapter(
            onItemClick = { /* Екранът „Детайли“ идва в следваща стъпка. */ },
        )
        binding.recyclerClothes.layoutManager =
            GridLayoutManager(requireContext(), resources.getInteger(R.integer.grid_columns))
        binding.recyclerClothes.adapter = adapter

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (!isUpdatingChips) viewModel.selectType(chipToType[checkedIds.firstOrNull()])
        }
        binding.chipGroupSubtype.setOnCheckedStateChangeListener { group, checkedIds ->
            if (isUpdatingChips) return@setOnCheckedStateChangeListener
            val chip = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            viewModel.selectSubtype(chip?.tag as ClothingSubtype?)
        }

        // Слушаме ViewModel-а само докато екранът е видим (STARTED).
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(viewModel.selectedType, viewModel.selectedSubtype) { t, s -> t to s }
                        .collect { (type, subtype) -> showFilters(type, subtype) }
                }
                launch {
                    viewModel.clothes.collect { clothes -> render(clothes, adapter) }
                }
            }
        }
    }

    /** Синхронизира чиповете с филтрите от ViewModel-а (напр. след завъртане на екрана). */
    private fun showFilters(type: ClothingType?, subtype: ClothingSubtype?) {
        isUpdatingChips = true

        val typeChipId = chipToType.entries.first { it.value == type }.key
        if (binding.chipGroupFilter.checkedChipId != typeChipId) {
            binding.chipGroupFilter.check(typeChipId)
        }

        if (type != subtypeRowType || binding.chipGroupSubtype.childCount == 0) {
            buildSubtypeRow(type)
        }
        binding.chipGroupSubtype.children
            .map { it as Chip }
            .firstOrNull { it.tag == subtype }
            ?.isChecked = true

        isUpdatingChips = false
    }

    /** Създава чиповете „Всички · Дънки · Клин · …“ за избрания тип; скрива реда при „Всички“. */
    private fun buildSubtypeRow(type: ClothingType?) {
        // Плавна анимация при показване/скриване на реда.
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)

        subtypeRowType = type
        val group = binding.chipGroupSubtype
        group.removeAllViews()
        binding.scrollSubtypes.isVisible = type != null
        if (type == null) return

        val subtypes = listOf<ClothingSubtype?>(null) + ClothingSubtype.forType(type)
        for (subtype in subtypes) {
            val chip = layoutInflater.inflate(R.layout.item_filter_chip, group, false) as Chip
            chip.id = View.generateViewId()
            chip.tag = subtype
            chip.setText(subtype?.labelRes ?: R.string.filter_all)
            group.addView(chip)
        }
        binding.scrollSubtypes.scrollTo(0, 0)
    }

    /** Показва списъка, празното състояние или зареждане (clothes == null). */
    private fun render(clothes: List<ClothingItem>?, adapter: ClothingAdapter) {
        binding.progress.isVisible = clothes == null
        if (clothes == null) return

        adapter.submitList(clothes)
        binding.textCount.text =
            resources.getQuantityString(R.plurals.clothes_count, clothes.size, clothes.size)

        val isEmpty = clothes.isEmpty()
        binding.layoutEmpty.isVisible = isEmpty
        binding.recyclerClothes.isVisible = !isEmpty
        binding.textCount.isVisible = !isEmpty

        if (isEmpty) {
            val filtered = viewModel.selectedType.value != null
            binding.textEmptyTitle.setText(
                if (filtered) R.string.wardrobe_empty_filter_title else R.string.wardrobe_empty_title,
            )
            binding.textEmptySubtitle.setText(
                if (filtered) R.string.wardrobe_empty_filter_subtitle else R.string.wardrobe_empty_subtitle,
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subtypeRowType = null
        _binding = null
    }
}
