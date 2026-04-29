package hu.bme.aut.android.demo.data.teammatch.model

import kotlinx.serialization.Serializable

/**
 * DTO egy adott játékos meccshez adásához (API kérés).
 */
@Serializable
class AddParticipantDTO (
    val userId: Int
)