package hu.bme.aut.android.demo.data.network.model.teamMatch

import kotlinx.serialization.Serializable

@Serializable
data class ScoreSubmitDTO(
    val homeScore: Int,
    val guestScore: Int,
    val setScores: String,
    val status: String
)