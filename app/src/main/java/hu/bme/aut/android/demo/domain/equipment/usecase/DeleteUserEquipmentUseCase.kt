package hu.bme.aut.android.demo.domain.equipment.usecase

import hu.bme.aut.android.demo.domain.equipment.repository.EquipmentRepository
import javax.inject.Inject

/** UseCase egy meglévő ütő törléséhez a felhasználó profiljából. */
class DeleteUserEquipmentUseCase @Inject constructor(
    private val repository: EquipmentRepository
) {
    suspend operator fun invoke(racketId: Int) = repository.deleteEquipment(racketId)
}