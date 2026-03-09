package hu.bme.aut.android.demo.domain.teammatch.model

data class IndividualMatch(
    val id: Int,
    val homePlayerName: String,
    val guestPlayerName: String,
    val homeScore: Int,
    val guestScore: Int,
    val setScores: String?,
    val status: String,
    val orderNumber: Int = 0
)