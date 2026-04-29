package hu.bme.aut.android.demo.domain.equipment.usecase

import hu.bme.aut.android.demo.domain.equipment.model.Equipment
import hu.bme.aut.android.demo.domain.equipment.repository.EquipmentRepository
import jakarta.inject.Inject

/** UseCase egy konkrét ütő adatainak lekérdezéséhez ID alapján. */
class GetEquipmentByIdUseCase @Inject constructor(
    private val repository: EquipmentRepository
) {
    suspend operator fun invoke(racketId: Int): Equipment? = repository.getEquipmentById(racketId)
}