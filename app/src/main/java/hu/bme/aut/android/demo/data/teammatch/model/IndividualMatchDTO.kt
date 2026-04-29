package hu.bme.aut.android.demo.data.teammatch.model

import kotlinx.serialization.Serializable

/**
 * DTO egy csapatmérkőzésen belüli egyéni meccs fogadásához (API válasz).
 */
@Serializable
data class IndividualMatchDTO(
    val id: Int,
    val homePlayerId: Int,
    val homePlayerName: String,
    val guestPlayerId: Int,
    val guestPlayerName: String,
    val homeScore: Int, // Hazai által nyert szettek száma (0-3)
    val guestScore: Int, // Vendég által nyert szettek száma (0-3)
    val setScores: String? = null, // Pl. "11-8, 9-11, 11-5, 11-6"
    val status: String? = "pending", // pending, in_progress, finished
    val orderNumber: Int = 0
)