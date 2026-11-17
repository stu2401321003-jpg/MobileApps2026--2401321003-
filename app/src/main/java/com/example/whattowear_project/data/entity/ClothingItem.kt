package com.example.whattowear_project.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Една дреха от гардероба = един ред в таблицата "clothing_items".
 */
@Entity(tableName = "clothing_items")
data class ClothingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Име, напр. „Черно зимно яке“. */
    val name: String,
    val type: ClothingType,
    /** Подвид, напр. „Дънки“. Nullable - дрехите от версия 1 на базата нямат подвид. */
    val subtype: ClothingSubtype? = null,
    /** Най-ниската температура (°C), при която дрехата е подходяща. */
    val minTemp: Int,
    /** Най-високата температура (°C), при която дрехата е подходяща. */
    val maxTemp: Int,
    /** Става ли за дъжд (непромокаемо яке, ботуши, чадър). */
    val waterproof: Boolean = false,
    /** Път до снимката във вътрешната памет. В базата пазим само пътя, не самата снимка. */
    val photoPath: String? = null,
)
