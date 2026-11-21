package com.example.whattowear_project.data.entity

/**
 * Подвид на дрехата (втора степен на класификация).
 * Всеки подвид знае към кой основен тип принадлежи - така не може да има „Яке → Дънки“.
 *
 * Основният тип (ClothingType) се ползва за правилата за тоалета,
 * а подвидът - за по-точно описание и филтриране в гардероба.
 *
 * Всеки подвид носи и ПРЕДЛОЖЕНИЕ за температурен диапазон и дали става за дъжд -
 * формата за нова дреха ги попълва автоматично (потребителят може да ги промени).
 *
 * ВАЖНО: Room пази името на константата (напр. "JEANS") - не ги преименувай.
 */
enum class ClothingSubtype(
    val type: ClothingType,
    val suggestedMinTemp: Int,
    val suggestedMaxTemp: Int,
    val suggestedWaterproof: Boolean = false,
) {
    // Горнища
    T_SHIRT(ClothingType.TOP, 18, 40),
    BLOUSE(ClothingType.TOP, 14, 28),
    SHIRT(ClothingType.TOP, 12, 28),
    SWEATER(ClothingType.TOP, -5, 15),
    SWEATSHIRT(ClothingType.TOP, 5, 18),

    // Долнища
    JEANS(ClothingType.BOTTOM, -5, 25),
    TROUSERS(ClothingType.BOTTOM, 0, 25),
    LEGGINGS(ClothingType.BOTTOM, 0, 22),
    SKIRT(ClothingType.BOTTOM, 16, 35),
    SHORTS(ClothingType.BOTTOM, 22, 40),

    // Якета
    JACKET(ClothingType.JACKET, -5, 15),
    COAT(ClothingType.JACKET, -20, 8),
    TRENCH_COAT(ClothingType.JACKET, 5, 18, suggestedWaterproof = true),
    VEST(ClothingType.JACKET, 5, 16),

    // Обувки
    SNEAKERS(ClothingType.SHOES, 5, 35),
    BOOTS(ClothingType.SHOES, -20, 10, suggestedWaterproof = true),
    SHOES(ClothingType.SHOES, 5, 28),
    SANDALS(ClothingType.SHOES, 22, 40),

    // Аксесоари
    UMBRELLA(ClothingType.ACCESSORY, -10, 35, suggestedWaterproof = true),
    HAT(ClothingType.ACCESSORY, -20, 5),
    SCARF(ClothingType.ACCESSORY, -20, 8),
    GLOVES(ClothingType.ACCESSORY, -25, 5),
    ;

    companion object {
        /** Всички подвидове на даден основен тип, в реда на дефиниране. */
        fun forType(type: ClothingType): List<ClothingSubtype> = entries.filter { it.type == type }
    }
}
