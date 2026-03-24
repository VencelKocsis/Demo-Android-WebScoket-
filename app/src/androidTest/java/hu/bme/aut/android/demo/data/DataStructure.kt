package hu.bme.aut.android.demo.data

object DataStructure {

    data class CaptainTestUser(
        val email: String,
        val password: String
    )

    data class RegularTestUser(
        val name: String
    )

    data class FullTestUser(
        val name: String,
        val email: String,
        val password: String
    )

    data class TeamData<T>(
        val id: Int = -1,
        val teamName: String,
        val captain: CaptainTestUser,
        val members: List<T>
    )

    data class TeamMatchData<T>(
        val id: Int = -1,
        val homeTeam: TeamData<T>,
        val guestTeam: TeamData<T>
    )
}