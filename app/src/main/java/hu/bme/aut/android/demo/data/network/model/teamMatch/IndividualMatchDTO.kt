package hu.bme.aut.android.demo.data.network.model.teamMatch

import kotlinx.serialization.Serializable

@Serializable
data class IndividualMatchDTO(
    val id: Int,
    val homePlayerName: String, // TODO make userId
    val guestPlayerName: String, // TODO make userId
    val homeScore: Int,
    val guestScore: Int
)
