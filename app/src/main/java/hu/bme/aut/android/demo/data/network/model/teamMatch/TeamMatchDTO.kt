package hu.bme.aut.android.demo.data.network.model.teamMatch

import kotlinx.serialization.Serializable

@Serializable
data class TeamMatchDTO(
    val id: Int,
    val roundNumber: Int,
    val homeTeamName: String = "Hazai csapat",
    val guestTeamName: String = "Vendég csapat",
    val homeScore: Int = 0,
    val guestScore: Int = 0,
    val date: String?,
    val status: String,
    val location: String? = null,
    val seasonId: Int = 0,
    val homeTeamId: Int = 0,
    val guestTeamId: Int = 0
    // TODO home and guest team members list
)