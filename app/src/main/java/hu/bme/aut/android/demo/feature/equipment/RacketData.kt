package hu.bme.aut.android.demo.feature.equipment

import androidx.compose.runtime.Immutable
 // TODO replace and delete
@Immutable
data class Racket(
    val blade: String,
    val forehandRubber: String,
    val backhandRubber: String
)

@Immutable
data class Blade(
    val manufacturer: String = "",
    val style: String = "Regular",
    val layers: Int = 5,
    val weight: Float = 85f,
    val type: String = "OFF",
    val thickness: Float = 6.0f,
    var model: String = ""
)

@Immutable
data class Rubber(
    val manufacturer: String = "",
    var model: String = "",
    val type: String = "Inverted",
    val hardness: Int = 40,
    val spongeThickness: Float = 2.1f,
    val color: String = "Black"
)