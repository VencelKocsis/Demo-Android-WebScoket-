package hu.bme.aut.android.demo.data.team.model

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) a csapat nevének szerkesztéséhez (API kérés).
 * * Csomagoló osztály, mert a backend egy JSON objektumot vár {"name": "Új név"} formátumban.
 */
@Serializable
data class TeamUpdateDTO(
    val name: String
)

/**
 * Data Transfer Object (DTO) egy játékos csapathoz adásához (API kérés).
 * * Csomagoló osztály a userId elküldéséhez.
 */
@Serializable
data class TeamMemberOperationDTO(
    val userId: Int
)