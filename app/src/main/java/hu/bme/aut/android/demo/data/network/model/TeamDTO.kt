package hu.bme.aut.android.demo.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamWithMembersDTO(
    val teamId: Int,
    val teamName: String,
    val clubName: String,
    val division: String? = null,
    val members: List<MemberDTO>
)

@Serializable
data class MemberDTO(
    val userId: Int,
    val name: String,
    val isCaptain: Boolean
)