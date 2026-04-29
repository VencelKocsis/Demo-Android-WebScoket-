package hu.bme.aut.android.demo.data.teammatch.model

import kotlinx.serialization.Serializable

/**
 * DTO a teljes csapatmérkőzés (TeamMatch) adatainak fogadására (API válasz).
 * * Bonyolult fa struktúra, ami listákat (IndividualMatches, Participants) is tartalmaz.
 */
@Serializable
data class TeamMatchDTO(
    val id: Int,
    val seasonId: Int,
    val seasonName: String? = "Ismeretlen szezon",
    val roundNumber: Int,
    val homeTeamName: String = "Hazai csapat",
    val guestTeamName: String = "Vendég csapat",
    val homeScore: Int = 0,
    val guestScore: Int = 0,
    val date: String?,
    val status: String,
    val location: String? = null,
    val homeTeamId: Int = 0,
    val guestTeamId: Int = 0,
    val individualMatches: List<IndividualMatchDTO>? = emptyList(),
    val participants: List<MatchParticipantDTO>? = emptyList(),
    val homeTeamSigned: Boolean = false,
    val guestTeamSigned: Boolean = false
)
