package hu.bme.aut.android.demo.data.equipment.model

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) egy asztalitenisz ütő adatainak hálózati küldéséhez és fogadásához.
 * * Csak a Data és Network rétegek használják!
 */
@Serializable
data class RacketDTO(
    val id: Int? = null,
    val bladeManufacturer: String,
    val bladeModel: String,
    val fhRubberManufacturer: String,
    val fhRubberModel: String,
    val fhRubberColor: String,
    val bhRubberManufacturer: String,
    val bhRubberModel: String,
    val bhRubberColor: String,
    val isForSale: Boolean = false
)