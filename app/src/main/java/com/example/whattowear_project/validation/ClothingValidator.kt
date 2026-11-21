package com.example.whattowear_project.validation

import com.example.whattowear_project.data.entity.ClothingSubtype
import com.example.whattowear_project.data.entity.ClothingType

/** Възможните грешки във формата за дреха. */
enum class ClothingFormError {
    NAME_EMPTY,
    NAME_TOO_LONG,
    TYPE_MISSING,
    SUBTYPE_MISMATCH,
    TEMP_RANGE_INVALID,
}

/**
 * Проверява данните от формата, преди да ги запишем в базата.
 * Чиста логика - без Android и без база, затова се тества лесно с Unit тестове.
 */
object ClothingValidator {

    const val MAX_NAME_LENGTH = 40
    const val MIN_TEMP = -30
    const val MAX_TEMP = 40

    /** Връща всички открити грешки; празно множество = всичко е наред. */
    fun validate(
        name: String,
        type: ClothingType?,
        subtype: ClothingSubtype?,
        minTemp: Int,
        maxTemp: Int,
    ): Set<ClothingFormError> = buildSet {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) add(ClothingFormError.NAME_EMPTY)
        if (trimmed.length > MAX_NAME_LENGTH) add(ClothingFormError.NAME_TOO_LONG)

        if (type == null) add(ClothingFormError.TYPE_MISSING)
        // Подвидът трябва да е от избрания тип (напр. не „Яке → Дънки“).
        if (type != null && subtype != null && subtype.type != type) {
            add(ClothingFormError.SUBTYPE_MISMATCH)
        }

        if (minTemp > maxTemp || minTemp < MIN_TEMP || maxTemp > MAX_TEMP) {
            add(ClothingFormError.TEMP_RANGE_INVALID)
        }
    }
}
