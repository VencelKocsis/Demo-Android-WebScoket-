package hu.bme.aut.android.demo.data.teammatch.model

import kotlinx.serialization.Serializable

/**
 * DTO egy egyéni mérkőzés eredményének beküldéséhez (API kérés).
 */
@Serializable
data class ScoreSubmitDTO(
    val homeScore: Int,
    val guestScore: Int,
    val setScores: String,
    val status: String
)