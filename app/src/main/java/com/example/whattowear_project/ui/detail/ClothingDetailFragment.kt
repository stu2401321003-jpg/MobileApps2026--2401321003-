package com.example.whattowear_project.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.whattowear_project.R
import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.databinding.FragmentClothingDetailBinding
import com.example.whattowear_project.ui.common.NavArgs
import com.example.whattowear_project.ui.common.applyBottomSystemInsets
import com.example.whattowear_project.ui.common.iconRes
import com.example.whattowear_project.ui.common.labelRes
import com.example.whattowear_project.ui.common.showClothingImage
import com.example.whattowear_project.validation.ClothingValidator
import com.example.whattowear_project.viewmodel.ClothingDetailViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * „Детайли“: една дреха в голям размер, с бутони „Редактирай“ и „Изтрий“.
 */
@AndroidEntryPoint
class ClothingDetailFragment : Fragment() {

    private var _binding: FragmentClothingDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClothingDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentClothingDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.applyBottomSystemInsets()

        binding.info.textScaleMin.text = getString(R.string.temp_value, ClothingValidator.MIN_TEMP)
        binding.info.textScaleMax.text = getString(R.string.temp_value, ClothingValidator.MAX_TEMP)

        binding.info.buttonEdit.setOnClickListener {
            findNavController().navigate(
                R.id.action_detail_to_edit,
                bundleOf(NavArgs.ITEM_ID to viewModel.itemId),
            )
        }
        binding.info.buttonDelete.setOnClickListener { confirmDelete() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.item.collect { item -> if (item != null) render(item) } }
                launch {
                    viewModel.deleted.collect { deleted ->
                        if (deleted) {
                            val root = requireActivity().findViewById<View>(android.R.id.content)
                            Snackbar.make(root, R.string.detail_deleted, Snackbar.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    private fun render(item: ClothingItem) {
        // Снимка или цветен фон + икона на типа.
        showClothingImage(item, binding.photo.root, binding.photo.imageTypeIcon, binding.photo.imagePhoto)

        // Име и етикети.
        binding.info.textName.text = item.name
        binding.info.chipKind.setText(item.subtype?.labelRes ?: item.type.labelRes)
        binding.info.chipKind.setChipIconResource(item.type.iconRes)
        binding.info.chipWaterproof.isVisible = item.waterproof

        // Температура: текст + цветна скала.
        binding.info.textTempRange.text = getString(R.string.temp_range, item.minTemp, item.maxTemp)
        binding.info.textTempDescription.text =
            getString(R.string.detail_temp_description, item.minTemp, item.maxTemp)
        showTemperatureBar(item.minTemp, item.maxTemp)
    }

    /**
     * Скалата е [преди][диапазон][след] с тегла в градуси.
     * Напр. -20° … 8° на скала -30° … 40° → 10 : 28 : 32.
     */
    private fun showTemperatureBar(minTemp: Int, maxTemp: Int) {
        val scaleMin = ClothingValidator.MIN_TEMP
        val scaleMax = ClothingValidator.MAX_TEMP
        val before = (minTemp - scaleMin).coerceAtLeast(0)
        // Поне 1 градус, за да се вижда и диапазон от една стойност (напр. 5° … 5°).
        val range = (maxTemp - minTemp).coerceAtLeast(1)
        val after = (scaleMax - maxTemp).coerceAtLeast(0)

        setWeight(binding.info.tempBarBefore, before)
        setWeight(binding.info.tempBarRange, range)
        setWeight(binding.info.tempBarAfter, after)
        binding.info.tempBar.requestLayout()
    }

    private fun setWeight(view: View, weight: Int) {
        (view.layoutParams as LinearLayout.LayoutParams).weight = weight.toFloat()
    }

    /** Диалог за потвърждение - изтриването не може да се отмени. */
    private fun confirmDelete() {
        val name = viewModel.item.value?.name ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_delete)
            .setTitle(R.string.delete_dialog_title)
            .setMessage(getString(R.string.delete_dialog_message, name))
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.delete_dialog_confirm) { _, _ -> viewModel.delete() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
