package hu.bme.aut.android.demo.domain.equipment.usecase

import hu.bme.aut.android.demo.domain.equipment.model.Equipment
import hu.bme.aut.android.demo.domain.equipment.repository.EquipmentRepository
import javax.inject.Inject

class SaveUserEquipmentUseCase @Inject constructor(
    private val repository: EquipmentRepository
) {
    suspend operator fun invoke(racket: Equipment) {
        repository.saveEquipment(racket)
    }
}