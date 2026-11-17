package com.example.whattowear_project.ui.common

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.whattowear_project.R
import com.example.whattowear_project.data.entity.ClothingSubtype
import com.example.whattowear_project.data.entity.ClothingType

/**
 * Как изглежда всеки тип дреха на екрана: текст, икона и цвят.
 * Пазим го на едно място, за да е еднакво навсякъде (гардероб, детайли, начало).
 */

@get:StringRes
val ClothingType.labelRes: Int
    get() = when (this) {
        ClothingType.TOP -> R.string.type_top
        ClothingType.BOTTOM -> R.string.type_bottom
        ClothingType.JACKET -> R.string.type_jacket
        ClothingType.SHOES -> R.string.type_shoes
        ClothingType.ACCESSORY -> R.string.type_accessory
    }

@get:DrawableRes
val ClothingType.iconRes: Int
    get() = when (this) {
        ClothingType.TOP -> R.drawable.ic_type_top
        ClothingType.BOTTOM -> R.drawable.ic_type_bottom
        ClothingType.JACKET -> R.drawable.ic_type_jacket
        ClothingType.SHOES -> R.drawable.ic_type_shoes
        ClothingType.ACCESSORY -> R.drawable.ic_type_accessory
    }

@get:ColorRes
val ClothingType.colorRes: Int
    get() = when (this) {
        ClothingType.TOP -> R.color.type_top
        ClothingType.BOTTOM -> R.color.type_bottom
        ClothingType.JACKET -> R.color.type_jacket
        ClothingType.SHOES -> R.color.type_shoes
        ClothingType.ACCESSORY -> R.color.type_accessory
    }

@get:StringRes
val ClothingSubtype.labelRes: Int
    get() = when (this) {
        ClothingSubtype.T_SHIRT -> R.string.subtype_t_shirt
        ClothingSubtype.BLOUSE -> R.string.subtype_blouse
        ClothingSubtype.SHIRT -> R.string.subtype_shirt
        ClothingSubtype.SWEATER -> R.string.subtype_sweater
        ClothingSubtype.SWEATSHIRT -> R.string.subtype_sweatshirt
        ClothingSubtype.JEANS -> R.string.subtype_jeans
        ClothingSubtype.TROUSERS -> R.string.subtype_trousers
        ClothingSubtype.LEGGINGS -> R.string.subtype_leggings
        ClothingSubtype.SKIRT -> R.string.subtype_skirt
        ClothingSubtype.SHORTS -> R.string.subtype_shorts
        ClothingSubtype.JACKET -> R.string.subtype_jacket
        ClothingSubtype.COAT -> R.string.subtype_coat
        ClothingSubtype.TRENCH_COAT -> R.string.subtype_trench_coat
        ClothingSubtype.VEST -> R.string.subtype_vest
        ClothingSubtype.SNEAKERS -> R.string.subtype_sneakers
        ClothingSubtype.BOOTS -> R.string.subtype_boots
        ClothingSubtype.SHOES -> R.string.subtype_shoes
        ClothingSubtype.SANDALS -> R.string.subtype_sandals
        ClothingSubtype.UMBRELLA -> R.string.subtype_umbrella
        ClothingSubtype.HAT -> R.string.subtype_hat
        ClothingSubtype.SCARF -> R.string.subtype_scarf
        ClothingSubtype.GLOVES -> R.string.subtype_gloves
    }
