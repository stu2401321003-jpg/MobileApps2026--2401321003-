package com.example.whattowear_project.di

import android.content.Context
import androidx.room.Room
import com.example.whattowear_project.data.WardrobeDatabase
import com.example.whattowear_project.data.dao.ClothingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt модул: „рецепти“ как се създават базата и DAO-то.
 * SingletonComponent = обектите живеят колкото цялото приложение.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** Една-единствена база за цялото приложение. */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WardrobeDatabase =
        Room.databaseBuilder(
            context,
            WardrobeDatabase::class.java,
            WardrobeDatabase.DATABASE_NAME,
        ).build()

    /** DAO-то го взимаме от базата. */
    @Provides
    fun provideClothingDao(database: WardrobeDatabase): ClothingDao = database.clothingDao()
}
