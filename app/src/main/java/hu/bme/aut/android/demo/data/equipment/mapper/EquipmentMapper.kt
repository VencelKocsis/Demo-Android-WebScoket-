package hu.bme.aut.android.demo.data.equipment.mapper

import hu.bme.aut.android.demo.data.equipment.model.RacketDTO
import hu.bme.aut.android.demo.domain.equipment.model.Equipment

/**
 * A tiszta Domain modellt alakítja át hálózati adatátviteli objektummá (DTO),
 * hogy a Retrofit el tudja küldeni a szervernek.
 */
fun Equipment.toDTO(): RacketDTO {
    return RacketDTO(
        id = this.id,
        bladeManufacturer = this.bladeManufacturer,
        bladeModel = this.bladeModel,
        fhRubberManufacturer = this.fhRubberManufacturer,
        fhRubberModel = this.fhRubberModel,
        fhRubberColor = this.fhRubberColor,
        bhRubberManufacturer = this.bhRubberManufacturer,
        bhRubberModel = this.bhRubberModel,
        bhRubberColor = this.bhRubberColor,
        isForSale = this.isForSale
    )
}

/**
 * A hálózati JSON modellt (DTO) alakítja át tiszta Domain modellé az üzleti logika számára.
 */
fun RacketDTO.toDomain(): Equipment {
    return Equipment(
        id = this.id,
        bladeManufacturer = this.bladeManufacturer,
        bladeModel = this.bladeModel,
        fhRubberManufacturer = this.fhRubberManufacturer,
        fhRubberModel = this.fhRubberModel,
        fhRubberColor = this.fhRubberColor,
        bhRubberManufacturer = this.bhRubberManufacturer,
        bhRubberModel = this.bhRubberModel,
        bhRubberColor = this.bhRubberColor,
        isForSale = this.isForSale
    )
}