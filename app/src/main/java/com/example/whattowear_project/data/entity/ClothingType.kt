package com.example.whattowear_project.data.entity

/**
 * Видовете дрехи. Room пази enum-а в базата като текст (напр. "JACKET").
 */
enum class ClothingType {
    TOP, // горнище: тениска, блуза, пуловер
    BOTTOM, // долнище: дънки, панталон, пола
    JACKET, // яке, палто
    SHOES, // обувки
    ACCESSORY, // аксесоар: чадър, шапка, шал
}
