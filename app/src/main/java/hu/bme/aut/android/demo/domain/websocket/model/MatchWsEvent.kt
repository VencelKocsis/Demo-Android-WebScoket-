package hu.bme.aut.android.demo.domain.websocket.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class MatchWsEvent {
    @Serializable
    @SerialName("IndividualScoreUpdated")
    data class IndividualScoreUpdated(
        val individualMatchId: Int,
        val homeScore: Int,
        val guestScore: Int,
        val setScores: String, // pl. "11-8, 9-11"
        val status: String
    ) : MatchWsEvent()

}