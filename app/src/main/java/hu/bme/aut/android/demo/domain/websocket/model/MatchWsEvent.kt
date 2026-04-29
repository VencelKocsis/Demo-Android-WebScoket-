package hu.bme.aut.android.demo.domain.websocket.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A mérkőzésekhez tartozó valós idejű WebSocket események tiszta (Domain) modellje.
 * * Pragmatikus kompromisszum: Bár a Domain rétegben vagyunk, a [@Serializable] annotációt
 * használjuk a rengeteg DTO mapping (sealed class másolás) elkerülése érdekében.
 */
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

    @Serializable
    @SerialName("MatchSignatureUpdated")
    data class MatchSignatureUpdated(
        val matchId: Int,
        val homeSigned: Boolean,
        val guestSigned: Boolean,
        val status: String
    ) : MatchWsEvent()
}