package hu.bme.aut.android.demo.data.teammatch.model

import kotlinx.serialization.Serializable

/**
 * DTO egy meccs résztvevőjének (játékos) fogadásához.
 */
@Serializable
data class MatchParticipantDTO(
    val id: Int,
    val userId: Int,               // Ezt a sorrend beküldéséhez használjuk
    val firebaseUid: String? = null, // Ezt a ViewModel-es azonosításhoz használjuk
    val playerName: String,
    val teamSide: String,
    val status: String,
    val position: Int? = null      // Ezt a UI visszatöltéséhez használjuk
)

/**
 * DTO egy játékos státuszának (pl. SELECTED, LOCKED) frissítéséhez (API kérés).
 */
@Serializable
data class ParticipantStatusUpdateDTO(val status: String)