package com.example.whattowear_project.outfit

import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.data.entity.ClothingType
import kotlin.math.abs

/** Защо дадена дреха е в тоалета. */
enum class PickReason {
    /** Температурният ѝ диапазон включва днешната температура. */
    FITS_TEMPERATURE,

    /** Под 5°C якето е задължително (дори ако диапазонът му не пасва идеално). */
    COLD_NEEDS_JACKET,

    /** Добавена заради дъжда (чадър / непромокаемо). */
    RAIN_PROTECTION,
}

data class OutfitPick(val item: ClothingItem, val reason: PickReason)

/** Препоръчаният тоалет + какво липсва в гардероба за днешното време. */
data class Outfit(
    val temperature: Int,
    val isRainy: Boolean,
    /** Подредени: яке, горнище, долнище, обувки, аксесоари. */
    val picks: List<OutfitPick>,
    /** Основни типове без подходяща дреха (горнище, долнище, обувки). */
    val missingTypes: List<ClothingType>,
    /** Под 5°C е, а в гардероба няма нито едно яке. */
    val missingJacket: Boolean,
    /** Вали, а няма нищо непромокаемо (чадър или непромокаемо яке). */
    val missingRainProtection: Boolean,
)

/**
 * ПРАВИЛАТА „температура → дрехи“. Чиста логика: без Android, база или интернет,
 * затова се тества лесно с Unit тестове.
 *
 * 1. Подходяща е дреха, чийто диапазон включва днешната температура (min ≤ t ≤ max).
 * 2. По една дреха от горнище, долнище и обувки - най-центрираната около t.
 * 3. Под 5°C якето е задължително; ако няма подходящо - най-топлото налично.
 * 4. При дъжд: предпочитаме непромокаеми дрехи; ако якето не е непромокаемо - добавяме чадър.
 * 5. Добавяме подходящите аксесоари (шапка, шал, ръкавици) - по един от вид.
 */
object OutfitRecommender {

    /** Под тази температура (°C) якето е задължително. */
    const val JACKET_REQUIRED_BELOW = 5

    private val MAIN_TYPES = listOf(ClothingType.TOP, ClothingType.BOTTOM, ClothingType.SHOES)

    /** Ред на показване в тоалета. */
    private val DISPLAY_ORDER = listOf(
        ClothingType.JACKET,
        ClothingType.TOP,
        ClothingType.BOTTOM,
        ClothingType.SHOES,
        ClothingType.ACCESSORY,
    )

    fun recommend(clothes: List<ClothingItem>, temperature: Int, isRainy: Boolean): Outfit {
        val picks = mutableListOf<OutfitPick>()
        val fitting = clothes.filter { temperature in it.minTemp..it.maxTemp }

        /**
         * Най-добрата от няколко кандидатки:
         * при дъжд първо непромокаемите → после най-центрираната около t → после по-тесният диапазон.
         */
        fun best(candidates: List<ClothingItem>): ClothingItem? = candidates.minWithOrNull(
            compareBy<ClothingItem>(
                { if (isRainy && !it.waterproof) 1 else 0 },
                { abs((it.minTemp + it.maxTemp) / 2.0 - temperature) },
                { it.maxTemp - it.minTemp },
                { it.id },
            ),
        )

        // --- Правило 3: якето ---
        val isCold = temperature < JACKET_REQUIRED_BELOW
        val jackets = clothes.filter { it.type == ClothingType.JACKET }
        val fittingJacket = best(fitting.filter { it.type == ClothingType.JACKET })
        var missingJacket = false
        when {
            fittingJacket != null -> picks += OutfitPick(
                fittingJacket,
                if (isCold) PickReason.COLD_NEEDS_JACKET else PickReason.FITS_TEMPERATURE,
            )
            isCold -> {
                // Няма идеално яке, но е студено - взимаме най-топлото (най-ниско minTemp).
                val warmest = jackets.minByOrNull { it.minTemp }
                if (warmest != null) {
                    picks += OutfitPick(warmest, PickReason.COLD_NEEDS_JACKET)
                } else {
                    missingJacket = true
                }
            }
        }

        // --- Правила 1 и 2: горнище, долнище, обувки ---
        val missingTypes = mutableListOf<ClothingType>()
        for (type in MAIN_TYPES) {
            val pick = best(fitting.filter { it.type == type })
            if (pick != null) picks += OutfitPick(pick, PickReason.FITS_TEMPERATURE) else missingTypes += type
        }

        // --- Правило 4: дъжд ---
        var missingRainProtection = false
        if (isRainy) {
            val hasWaterproofJacket = picks.any { it.item.type == ClothingType.JACKET && it.item.waterproof }
            if (!hasWaterproofJacket) {
                // Чадърът е полезен при всякаква температура - не проверяваме диапазона му.
                val umbrella = best(clothes.filter { it.type == ClothingType.ACCESSORY && it.waterproof })
                if (umbrella != null) {
                    picks += OutfitPick(umbrella, PickReason.RAIN_PROTECTION)
                } else {
                    missingRainProtection = true
                }
            }
        }

        // --- Правило 5: други аксесоари (шапка, шал, ръкавици) - по един от вид ---
        val chosenIds = picks.map { it.item.id }.toSet()
        fitting
            .filter { it.type == ClothingType.ACCESSORY && !it.waterproof && it.id !in chosenIds }
            .groupBy { it.subtype }
            .values
            .mapNotNull { best(it) }
            .forEach { picks += OutfitPick(it, PickReason.FITS_TEMPERATURE) }

        return Outfit(
            temperature = temperature,
            isRainy = isRainy,
            picks = picks.sortedBy { DISPLAY_ORDER.indexOf(it.item.type) },
            missingTypes = missingTypes,
            missingJacket = missingJacket,
            missingRainProtection = missingRainProtection,
        )
    }
}
