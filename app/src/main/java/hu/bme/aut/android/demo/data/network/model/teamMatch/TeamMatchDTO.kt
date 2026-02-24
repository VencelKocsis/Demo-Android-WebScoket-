package hu.bme.aut.android.demo.data.network.model.teamMatch

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TeamMatchDTO(
    val id: Int,
    @SerialName("season_id") val seasonId: Int,
    @SerialName("round_number") val roundNumber: Int, // Emiatt tudjuk majd csoportosítani!
    @SerialName("home_team_id") val homeTeamId: Int,
    @SerialName("guest_team_id") val guestTeamId: Int,

    // Remélhetőleg a backend ezeket is küldi (JOIN-olva), ha nem, vedd ki őket:
    @SerialName("home_team_name") val homeTeamName: String = "Hazai csapat",
    @SerialName("guest_team_name") val guestTeamName: String = "Vendég csapat",

    @SerialName("home_team_score") val homeTeamScore: Int = 0,
    @SerialName("guest_team_score") val guestTeamScore: Int = 0,

    val location: String?,
    @SerialName("match_date") val matchDate: String?, // Dátum stringként jön
    val status: String // "scheduled", "in_progress", "finished", "cancelled"
)