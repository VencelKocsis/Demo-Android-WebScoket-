package hu.bme.aut.android.demo.data.racket.model

import kotlinx.serialization.Serializable

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