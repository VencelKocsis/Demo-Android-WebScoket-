package hu.bme.aut.android.demo.data.network.model.teamMatch

import kotlinx.serialization.Serializable

@Serializable
data class LineupSubmitDTO(
    val teamSide: String, // "HOME" vagy "GUEST"
    val positions: Map<Int, Int> // Pozíció -> UserId
)