package com.example.whattowear_project.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.data.entity.ClothingSubtype
import com.example.whattowear_project.data.entity.ClothingType
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): всички заявки към таблицата с дрехите.
 * Room сам генерира кода на тези методи при компилация.
 */
@Dao
interface ClothingDao {

    // ---------- Read ----------

    /** Всички дрехи. Flow = екранът се обновява автоматично при всяка промяна в таблицата. */
    @Query("SELECT * FROM clothing_items ORDER BY name")
    fun getAll(): Flow<List<ClothingItem>>

    /** Само дрехите от даден тип (за филтъра в гардероба). */
    @Query("SELECT * FROM clothing_items WHERE type = :type ORDER BY name")
    fun getByType(type: ClothingType): Flow<List<ClothingItem>>

    /** Само дрехите от даден подвид (напр. само дънките). */
    @Query("SELECT * FROM clothing_items WHERE subtype = :subtype ORDER BY name")
    fun getBySubtype(subtype: ClothingSubtype): Flow<List<ClothingItem>>

    /** Една дреха по id (за екрана „Детайли“). */
    @Query("SELECT * FROM clothing_items WHERE id = :id")
    fun getById(id: Long): Flow<ClothingItem?>

    // ---------- Create / Update / Delete ----------
    // suspend = изпълнява се в корутина, не блокира главната нишка.

    /** Връща id-то на новата дреха. */
    @Insert
    suspend fun insert(item: ClothingItem): Long

    @Update
    suspend fun update(item: ClothingItem)

    @Delete
    suspend fun delete(item: ClothingItem)
}
