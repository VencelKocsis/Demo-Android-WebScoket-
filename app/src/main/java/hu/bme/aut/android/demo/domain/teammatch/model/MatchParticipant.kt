package hu.bme.aut.android.demo.domain.teammatch.model

/**
 * Egy mérkőzésre jelentkezett játékos (résztvevő) tiszta üzleti modellje.
 */
data class MatchParticipant(
    val id: Int,
    val userId: Int,
    val firebaseUid: String?,
    val playerName: String,
    val teamSide: String,
    val status: String,
    val position: Int? = null
)