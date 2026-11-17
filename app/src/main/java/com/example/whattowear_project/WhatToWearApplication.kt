package com.example.whattowear_project

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application класът се създава веднъж при старт на приложението, преди всеки екран.
 * @HiltAndroidApp стартира Hilt - оттук нататък той създава и подава зависимостите
 * (базата, DAO, Repository) където ни трябват.
 */
@HiltAndroidApp
class WhatToWearApplication : Application()
