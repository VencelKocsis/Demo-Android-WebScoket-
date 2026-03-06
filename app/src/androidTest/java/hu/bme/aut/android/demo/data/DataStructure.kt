package hu.bme.aut.android.demo.data

object DataStructure {

    data class CaptainTestUser(
        val email: String,
        val password: String
    )

    data class RegularTestUser(
        val name: String
    )

    data class TeamData(
        val captain: CaptainTestUser,
        val members: List<RegularTestUser>
    )
}