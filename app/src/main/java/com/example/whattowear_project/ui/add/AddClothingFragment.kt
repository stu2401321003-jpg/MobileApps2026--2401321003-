package com.example.whattowear_project.ui.add

import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.whattowear_project.R
import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.data.entity.ClothingSubtype
import com.example.whattowear_project.data.entity.ClothingType
import com.example.whattowear_project.databinding.FragmentAddClothingBinding
import com.example.whattowear_project.ui.common.applyBottomSystemInsets
import com.example.whattowear_project.ui.common.checkSubtypeChip
import com.example.whattowear_project.ui.common.checkedSubtype
import com.example.whattowear_project.ui.common.setSubtypeChips
import com.example.whattowear_project.validation.ClothingFormError
import com.example.whattowear_project.viewmodel.AddClothingViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Форма „Нова дреха“. Фрагментът показва полетата и праща действията към ViewModel-а.
 */
@AndroidEntryPoint
class AddClothingFragment : Fragment() {

    private var _binding: FragmentAddClothingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddClothingViewModel by viewModels()

    private val chipToType = mapOf(
        R.id.chip_type_top to ClothingType.TOP,
        R.id.chip_type_bottom to ClothingType.BOTTOM,
        R.id.chip_type_jacket to ClothingType.JACKET,
        R.id.chip_type_shoes to ClothingType.SHOES,
        R.id.chip_type_accessory to ClothingType.ACCESSORY,
    )

    /** За кой тип са построени чиповете с подвидове в момента. */
    private var subtypeRowType: ClothingType? = null

    /** true докато пренареждаме чиповете от кода - тогава не реагираме като на натискане. */
    private var isUpdatingChips = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddClothingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applyBottomSystemInsets()
        setupTemperatureSlider()

        // Една форма за две неща: „Нова дреха“ или „Редактиране“.
        if (viewModel.isEditing) {
            (requireActivity() as AppCompatActivity).supportActionBar
                ?.setTitle(R.string.title_edit_clothing)
            binding.buttonSave.setText(R.string.form_save_changes)
        }

        binding.editName.doAfterTextChanged { viewModel.onNameChanged() }

        binding.chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            if (!isUpdatingChips) viewModel.selectType(chipToType[checkedIds.firstOrNull()])
        }
        binding.chipGroupSubtype.setOnCheckedStateChangeListener { group, _ ->
            if (isUpdatingChips) return@setOnCheckedStateChangeListener
            val subtype = group.checkedSubtype
            viewModel.selectSubtype(subtype)
            if (subtype != null) applySuggestion(subtype)
        }

        // Цялата карта „Става за дъжд“ е натискаема, не само превключвателят.
        binding.cardWaterproof.setOnClickListener { binding.switchWaterproof.toggle() }

        binding.buttonSave.setOnClickListener {
            val (minTemp, maxTemp) = binding.sliderTemp.values.map { it.toInt() }
            viewModel.save(
                name = binding.editName.text?.toString().orEmpty(),
                minTemp = minTemp,
                maxTemp = maxTemp,
                waterproof = binding.switchWaterproof.isChecked,
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    combine(viewModel.selectedType, viewModel.selectedSubtype) { t, s -> t to s }
                        .collect { (type, subtype) -> showTypeAndSubtype(type, subtype) }
                }
                launch { viewModel.errors.collect { showErrors(it) } }
                launch { viewModel.itemToFill.collect { item -> if (item != null) fillForm(item) } }
                launch {
                    viewModel.saved.collect { saved ->
                        if (saved) {
                            val message =
                                if (viewModel.isEditing) R.string.form_saved_changes else R.string.form_saved
                            val root = requireActivity().findViewById<View>(android.R.id.content)
                            Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    /** Плъзгачът показва „-5°“, а голямото число отгоре - целия диапазон. */
    private fun setupTemperatureSlider() {
        binding.sliderTemp.setLabelFormatter { value -> getString(R.string.temp_value, value.toInt()) }
        binding.sliderTemp.addOnChangeListener { _, _, _ -> updateTempText() }
        updateTempText()
    }

    private fun updateTempText() {
        val (minTemp, maxTemp) = binding.sliderTemp.values.map { it.toInt() }
        binding.textTempRange.text = getString(R.string.temp_range, minTemp, maxTemp)
    }

    /** Попълва предложения диапазон и „дъжд“ за избрания подвид (напр. Палто → -20° … 8°). */
    private fun applySuggestion(subtype: ClothingSubtype) {
        binding.sliderTemp.values = listOf(
            subtype.suggestedMinTemp.toFloat(),
            subtype.suggestedMaxTemp.toFloat(),
        )
        binding.switchWaterproof.isChecked = subtype.suggestedWaterproof
    }

    private fun showTypeAndSubtype(type: ClothingType?, subtype: ClothingSubtype?) {
        isUpdatingChips = true

        val typeChipId = chipToType.entries.firstOrNull { it.value == type }?.key
        if (typeChipId == null) {
            binding.chipGroupType.clearCheck()
        } else if (binding.chipGroupType.checkedChipId != typeChipId) {
            binding.chipGroupType.check(typeChipId)
        }

        if (type != subtypeRowType || binding.chipGroupSubtype.childCount == 0) {
            TransitionManager.beginDelayedTransition(binding.root as ViewGroup)
            subtypeRowType = type
            binding.sectionSubtype.isVisible = type != null
            binding.chipGroupSubtype.setSubtypeChips(type?.let { ClothingSubtype.forType(it) }.orEmpty())
        }
        binding.chipGroupSubtype.checkSubtypeChip(subtype)

        isUpdatingChips = false
    }

    private fun showErrors(errors: Set<ClothingFormError>) {
        binding.inputLayoutName.error = when {
            ClothingFormError.NAME_EMPTY in errors -> getString(R.string.form_error_name_empty)
            ClothingFormError.NAME_TOO_LONG in errors -> getString(R.string.form_error_name_too_long)
            else -> null
        }
        binding.textTypeError.isVisible = ClothingFormError.TYPE_MISSING in errors
    }

    /**
     * Редакция: попълва полетата със стойностите на дрехата (само веднъж).
     * Типът и подвидът вече са във ViewModel-а - чиповете се маркират сами.
     */
    private fun fillForm(item: ClothingItem) {
        binding.editName.setText(item.name)
        binding.sliderTemp.values = listOf(item.minTemp.toFloat(), item.maxTemp.toFloat())
        binding.switchWaterproof.isChecked = item.waterproof
        viewModel.onFormFilled()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        subtypeRowType = null
        _binding = null
    }
}
