package hu.bme.aut.android.demo.data.network.model.teamMatch

import kotlinx.serialization.Serializable

@Serializable
data class MatchParticipantDTO(
    val id: Int,
    val userId: Int,
    val playerName: String,
    val teamSide: String, // HOME or GUEST
    val status: String, // APPLIED or SELECTED
)

@Serializable
data class ParticipantStatusUpdateDTO(val status: String)