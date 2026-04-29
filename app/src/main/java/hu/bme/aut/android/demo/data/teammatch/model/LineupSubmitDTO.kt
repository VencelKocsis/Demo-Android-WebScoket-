package hu.bme.aut.android.demo.data.teammatch.model

import kotlinx.serialization.Serializable

/**
 * DTO a kezdőcsapat / felállás (Lineup) beküldéséhez (API kérés).
 */
@Serializable
data class LineupSubmitDTO(
    val teamSide: String, // "HOME" vagy "GUEST"
    val positions: Map<Int, Int> // Pozíció -> UserId
)