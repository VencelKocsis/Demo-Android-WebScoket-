package hu.bme.aut.android.demo.feature.racketEditor

import androidx.compose.runtime.Immutable

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

val MODEL_DATA = mapOf(
    "Butterfly" to listOf("Timo Boll ALC", "Viscaria", "Innerforce Layer ZLC"),
    "DHS" to listOf("Ma Long 5", "Hurricane Long 5", "Power G9"),
    "Yasaka" to listOf("Ma Lin Extra Offensive", "Gatien Extra"),
    "Stiga" to listOf("Offensive Classic", "Carbonado 45"),
    "Tibhar" to listOf("Samsonov Force Pro", "Stratus Powerwood")
)

val RUBBER_MODEL_DATA = mapOf(
    "Butterfly" to listOf("Tenergy 05", "Dignics 09c", "Rozena"),
    "DHS" to listOf("Hurricane 3 NEO", "National Hurricane 3", "Skyline 3"),
    "Yasaka" to listOf("Rakza 7 Soft", "Rakza PO"),
    "Stiga" to listOf("Calibra LT", "Mantra S"),
    "Tibhar" to listOf("Evolution MX-P", "Quantum X Pro")
)

val RUBBER_COLORS = listOf("Red", "Black")
val MANUFACTURERS = MODEL_DATA.keys.toList()