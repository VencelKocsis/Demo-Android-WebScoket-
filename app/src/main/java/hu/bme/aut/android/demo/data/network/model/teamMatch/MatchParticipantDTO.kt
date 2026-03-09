package hu.bme.aut.android.demo.data.network.model.teamMatch

import kotlinx.serialization.Serializable

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

@Serializable
data class ParticipantStatusUpdateDTO(val status: String)