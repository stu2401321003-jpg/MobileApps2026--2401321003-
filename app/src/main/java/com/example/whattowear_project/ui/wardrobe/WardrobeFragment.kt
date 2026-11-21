package com.example.whattowear_project.ui.wardrobe

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whattowear_project.R
import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.data.entity.ClothingSubtype
import com.example.whattowear_project.data.entity.ClothingType
import com.example.whattowear_project.databinding.FragmentWardrobeBinding
import com.example.whattowear_project.ui.common.NavArgs
import com.example.whattowear_project.ui.common.checkSubtypeChip
import com.example.whattowear_project.ui.common.checkedSubtype
import com.example.whattowear_project.ui.common.setSubtypeChips
import com.example.whattowear_project.viewmodel.WardrobeViewModel
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
            onItemClick = { item ->
                findNavController().navigate(
                    R.id.action_wardrobe_to_detail,
                    bundleOf(NavArgs.ITEM_ID to item.id),
                )
            },
        )
        binding.recyclerClothes.layoutManager =
            GridLayoutManager(requireContext(), resources.getInteger(R.integer.grid_columns))
        binding.recyclerClothes.adapter = adapter

        // Бутоните „Добави“ отварят формата.
        val openAddForm = View.OnClickListener {
            findNavController().navigate(R.id.action_wardrobe_to_addClothing)
        }
        binding.fabAdd.setOnClickListener(openAddForm)
        binding.buttonAddFirst.setOnClickListener(openAddForm)

        // Плаващият бутон се свива при скрол надолу и се разгъва при скрол нагоре.
        binding.recyclerClothes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) binding.fabAdd.shrink() else if (dy < 0) binding.fabAdd.extend()
            }
        })

        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (!isUpdatingChips) viewModel.selectType(chipToType[checkedIds.firstOrNull()])
        }
        binding.chipGroupSubtype.setOnCheckedStateChangeListener { group, _ ->
            if (!isUpdatingChips) viewModel.selectSubtype(group.checkedSubtype)
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
        binding.chipGroupSubtype.checkSubtypeChip(subtype)

        isUpdatingChips = false
    }

    /** Създава чиповете „Всички · Дънки · Клин · …“ за избрания тип; скрива реда при „Всички“. */
    private fun buildSubtypeRow(type: ClothingType?) {
        // Плавна анимация при показване/скриване на реда.
        TransitionManager.beginDelayedTransition(binding.root as ViewGroup)

        subtypeRowType = type
        binding.scrollSubtypes.isVisible = type != null
        // null отпред = чип „Всички“.
        val subtypes = if (type == null) emptyList() else listOf(null) + ClothingSubtype.forType(type)
        binding.chipGroupSubtype.setSubtypeChips(subtypes)
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
