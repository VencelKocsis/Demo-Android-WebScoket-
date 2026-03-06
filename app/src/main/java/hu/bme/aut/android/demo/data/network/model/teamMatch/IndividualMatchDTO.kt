package hu.bme.aut.android.demo.data.network.model.teamMatch

import kotlinx.serialization.Serializable

@Serializable
data class IndividualMatchDTO(
    val id: Int,
    val homePlayerId: Int,
    val homePlayerName: String,
    val guestPlayerId: Int,
    val guestPlayerName: String,
    val homeScore: Int,
    val guestScore: Int
)
