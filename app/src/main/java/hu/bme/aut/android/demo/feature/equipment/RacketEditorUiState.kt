package hu.bme.aut.android.demo.feature.equipment

import androidx.compose.runtime.Immutable

/**
 * A UI számára szánt ideiglenes adatmodell egy fa (Blade) beállításaihoz.
 */
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

/**
 * A UI számára szánt ideiglenes adatmodell egy borítás (Rubber) beállításaihoz.
 */
@Immutable
data class Rubber(
    val manufacturer: String = "",
    var model: String = "",
    val type: String = "Inverted",
    val hardness: Int = 40,
    val spongeThickness: Float = 2.1f,
    val color: String = "Black"
)

/**
 * Az Ütő Szerkesztő (RacketEditorScreen) teljes, egyetlen igazságforrása.
 * * Tartalmazza a betöltési állapotokat, a legördülő menük választható elemeit,
 * valamint az éppen aktuálisan szerkesztett fa és borítások adatait.
 */
data class RacketEditorUiState(
    val isLoading: Boolean = true,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val racketId: Int? = null,
    val isForSale: Boolean = false,

    val bladeManufacturers: List<String> = emptyList(),
    val rubberManufacturers: List<String> = emptyList(),

    val availableBladeModels: List<String> = emptyList(),
    val availableFhModels: List<String> = emptyList(),
    val availableBhModels: List<String> = emptyList(),

    val rubberColors: List<String> = listOf("Red", "Black", "Blue", "Green", "Pink", "Purple"),

    val currentBlade: Blade = Blade(),
    val currentForehand: Rubber = Rubber(color = "Black"),
    val currentBackhand: Rubber = Rubber(color = "Red")
)