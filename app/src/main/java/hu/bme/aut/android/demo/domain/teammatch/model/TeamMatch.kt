package hu.bme.aut.android.demo.domain.teammatch.model

data class TeamMatch(
    val id: Int,
    val seasonId: Int,
    val seasonName: String?,
    val roundNumber: Int,
    val homeTeamId: Int,
    val guestTeamId: Int,
    val homeTeamName: String,
    val guestTeamName: String,
    val homeTeamScore: Int,
    val guestTeamScore: Int,
    val location: String?,
    val matchDate: String?,
    val status: String,
    val participants: List<MatchParticipant>,
    val individualMatches: List<IndividualMatch>,
    val homeTeamSigned: Boolean,
    val guestTeamSigned: Boolean
)