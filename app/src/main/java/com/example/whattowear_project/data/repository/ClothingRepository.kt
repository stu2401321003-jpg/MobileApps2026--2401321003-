package com.example.whattowear_project.data.repository

import com.example.whattowear_project.data.dao.ClothingDao
import com.example.whattowear_project.data.entity.ClothingItem
import com.example.whattowear_project.data.entity.ClothingSubtype
import com.example.whattowear_project.data.entity.ClothingType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository: единственото място, откъдето ViewModel-ите взимат дрехите.
 * ViewModel-ът не знае, че отдолу стои Room - така лесно можем да сменим
 * източника или да подадем фалшиво Repository в тестовете.
 *
 * @Inject constructor = Hilt сам създава Repository-то и му подава ClothingDao.
 */
@Singleton
class ClothingRepository @Inject constructor(
    private val clothingDao: ClothingDao,
) {

    /**
     * Дрехите според филтъра. Подвидът е по-точен, затова има предимство;
     * без филтри (null, null) връща всичко.
     */
    fun getClothes(
        type: ClothingType? = null,
        subtype: ClothingSubtype? = null,
    ): Flow<List<ClothingItem>> = when {
        subtype != null -> clothingDao.getBySubtype(subtype)
        type != null -> clothingDao.getByType(type)
        else -> clothingDao.getAll()
    }

    fun getById(id: Long): Flow<ClothingItem?> = clothingDao.getById(id)

    suspend fun add(item: ClothingItem): Long = clothingDao.insert(item)

    suspend fun update(item: ClothingItem) = clothingDao.update(item)

    suspend fun delete(item: ClothingItem) = clothingDao.delete(item)
}
