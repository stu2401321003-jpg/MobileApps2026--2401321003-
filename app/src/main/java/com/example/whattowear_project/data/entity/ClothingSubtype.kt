package com.example.whattowear_project.data.entity

/**
 * Подвид на дрехата (втора степен на класификация).
 * Всеки подвид знае към кой основен тип принадлежи - така не може да има „Яке → Дънки“.
 *
 * Основният тип (ClothingType) се ползва за правилата за тоалета,
 * а подвидът - за по-точно описание и филтриране в гардероба.
 *
 * ВАЖНО: Room пази името на константата (напр. "JEANS") - не ги преименувай.
 */
enum class ClothingSubtype(val type: ClothingType) {
    // Горнища
    T_SHIRT(ClothingType.TOP),
    BLOUSE(ClothingType.TOP),
    SHIRT(ClothingType.TOP),
    SWEATER(ClothingType.TOP),
    SWEATSHIRT(ClothingType.TOP),

    // Долнища
    JEANS(ClothingType.BOTTOM),
    TROUSERS(ClothingType.BOTTOM),
    LEGGINGS(ClothingType.BOTTOM),
    SKIRT(ClothingType.BOTTOM),
    SHORTS(ClothingType.BOTTOM),

    // Якета
    JACKET(ClothingType.JACKET),
    COAT(ClothingType.JACKET),
    TRENCH_COAT(ClothingType.JACKET),
    VEST(ClothingType.JACKET),

    // Обувки
    SNEAKERS(ClothingType.SHOES),
    BOOTS(ClothingType.SHOES),
    SHOES(ClothingType.SHOES),
    SANDALS(ClothingType.SHOES),

    // Аксесоари
    UMBRELLA(ClothingType.ACCESSORY),
    HAT(ClothingType.ACCESSORY),
    SCARF(ClothingType.ACCESSORY),
    GLOVES(ClothingType.ACCESSORY),
    ;

    companion object {
        /** Всички подвидове на даден основен тип, в реда на дефиниране. */
        fun forType(type: ClothingType): List<ClothingSubtype> = entries.filter { it.type == type }
    }
}
