package com.example.whattowear_project.ui.common

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * На вътрешните екрани (Нова дреха, Детайли) долната лента с табове е скрита,
 * затова изгледът сам оставя място отдолу за системната лента и клавиатурата (ime).
 */
fun View.applyBottomSystemInsets() {
    val basePadding = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bottom = insets.getInsets(
            WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.ime(),
        ).bottom
        view.updatePadding(bottom = basePadding + bottom)
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
