package com.example.whattowear_project.data

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.whattowear_project.data.dao.ClothingDao
import com.example.whattowear_project.data.entity.ClothingItem

/**
 * Самата Room база. Тук изброяваме таблиците (entities) и версията.
 *
 * История на версиите:
 *  1 - таблица clothing_items
 *  2 - нова колона subtype (подвид: дънки, клин, тениска...)
 *
 * Миграция 1 → 2 е автоматична: Room сравнява schemas/1.json и schemas/2.json
 * и сам генерира ALTER TABLE ... ADD COLUMN. Старите дрехи се запазват.
 */
@Database(
    entities = [ClothingItem::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class WardrobeDatabase : RoomDatabase() {

    abstract fun clothingDao(): ClothingDao

    companion object {
        const val DATABASE_NAME = "wardrobe.db"
    }
}
