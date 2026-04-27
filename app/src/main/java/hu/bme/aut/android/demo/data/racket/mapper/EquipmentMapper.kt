package hu.bme.aut.android.demo.data.racket.mapper

import hu.bme.aut.android.demo.data.racket.model.RacketDTO
import hu.bme.aut.android.demo.domain.equipment.model.Equipment

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