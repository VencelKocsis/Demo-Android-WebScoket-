package hu.bme.aut.android.demo.domain.equipment.model

data class Equipment(
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
