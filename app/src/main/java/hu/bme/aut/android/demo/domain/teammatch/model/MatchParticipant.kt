package hu.bme.aut.android.demo.domain.teammatch.model

data class MatchParticipant(
    val id: Int,
    val playerName: String,
    val teamSide: String, // HOME or GUEST
    val status: String, // APPLIED or SELECTED
)
