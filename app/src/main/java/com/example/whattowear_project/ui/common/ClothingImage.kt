package com.example.whattowear_project.ui.common

import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil3.load
import coil3.request.crossfade
import com.example.whattowear_project.data.entity.ClothingItem
import java.io.File

/**
 * Показва снимката на дрехата, а ако няма - цветен фон според типа + голяма икона.
 * Ползва се навсякъде (гардероб, детайли, тоалет), за да изглежда еднакво.
 *
 * @param container изгледът зад снимката - получава цвета на типа
 * @param typeIcon иконата на типа (вижда се само без снимка)
 * @param photo самата снимка (зарежда се с Coil)
 */
fun showClothingImage(item: ClothingItem, container: View, typeIcon: ImageView, photo: ImageView) {
    container.setBackgroundColor(ContextCompat.getColor(container.context, item.type.colorRes))
    typeIcon.setImageResource(item.type.iconRes)

    val photoPath = item.photoPath
    photo.isVisible = photoPath != null
    typeIcon.isVisible = photoPath == null
    if (photoPath != null) {
        // Coil зарежда снимката на заден план, смалява я до размера на изгледа и я кешира.
        photo.load(File(photoPath)) { crossfade(true) }
    }
}
