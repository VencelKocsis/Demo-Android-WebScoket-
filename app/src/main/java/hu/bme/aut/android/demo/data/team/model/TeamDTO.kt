package hu.bme.aut.android.demo.data.team.model

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) egy csapat részletes adatainak fogadására a backendtől.
 * * A [@Serializable] annotáció utasítja a JSON konvertert a szerializációra.
 */
@Serializable
data class TeamWithMembersDTO(
    val teamId: Int,
    val teamName: String,
    val clubName: String,
    val division: String? = null,
    val members: List<MemberDTO>,
    val matchesPlayed: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
    val points: Int
)

/**
 * Data Transfer Object (DTO) egy csapattag adatainak fogadására.
 */
@Serializable
data class MemberDTO(
    val userId: Int,
    val firebaseUid: String,
    val name: String,
    val isCaptain: Boolean
)