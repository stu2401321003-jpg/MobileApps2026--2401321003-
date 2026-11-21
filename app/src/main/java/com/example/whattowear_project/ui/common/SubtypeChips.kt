package com.example.whattowear_project.ui.common

import android.view.LayoutInflater
import android.view.View
import androidx.core.view.children
import com.example.whattowear_project.R
import com.example.whattowear_project.data.entity.ClothingSubtype
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Общи помощници за коралови чипове с подвидове (ползват се в гардероба и във формата).
 * Всеки чип пази своя подвид в `tag` (null = „Всички“).
 */

/** Заменя чиповете в групата с нови - по един за всеки подвид от списъка. */
fun ChipGroup.setSubtypeChips(subtypes: List<ClothingSubtype?>) {
    removeAllViews()
    val inflater = LayoutInflater.from(context)
    for (subtype in subtypes) {
        val chip = inflater.inflate(R.layout.item_filter_chip, this, false) as Chip
        chip.id = View.generateViewId()
        chip.tag = subtype
        chip.setText(subtype?.labelRes ?: R.string.filter_all)
        addView(chip)
    }
}

/** Маркира чипа на дадения подвид; ако няма такъв - маха избора. */
fun ChipGroup.checkSubtypeChip(subtype: ClothingSubtype?) {
    val chip = children.map { it as Chip }.firstOrNull { it.tag == subtype }
    if (chip != null) chip.isChecked = true else clearCheck()
}

/** Подвидът на избрания чип (null, ако няма избран или е „Всички“). */
val ChipGroup.checkedSubtype: ClothingSubtype?
    get() = findViewById<Chip>(checkedChipId)?.tag as ClothingSubtype?
