package com.example.whattowear_project.ui.common

/**
 * Имена на аргументите между екраните (същите като в nav_graph.xml).
 * Navigation ги слага автоматично в SavedStateHandle на ViewModel-а.
 */
object NavArgs {
    /** id на дрехата. За формата: NO_ID = нова дреха, друго = редактиране. */
    const val ITEM_ID = "itemId"
    const val NO_ID = -1L
}
