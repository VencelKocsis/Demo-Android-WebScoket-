package hu.bme.aut.android.demo.data.network.model.team

import kotlinx.serialization.Serializable

@Serializable
data class TeamUpdateDTO(
    val name: String
)

@Serializable
data class TeamMemberOperationDTO(
    val userId: Int
)