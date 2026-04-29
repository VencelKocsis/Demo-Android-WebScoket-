package hu.bme.aut.android.demo.data.racket.repository

import hu.bme.aut.android.demo.data.network.api.auth.AuthApiService
import hu.bme.aut.android.demo.data.network.api.equipment.EquipmentApiService
import hu.bme.aut.android.demo.data.racket.mapper.toDTO
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.equipment.model.Equipment
import hu.bme.aut.android.demo.domain.equipment.repository.EquipmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentRepositoryImpl @Inject constructor(
    private val equipmentApiService: EquipmentApiService,
    private val authApiService: AuthApiService,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : EquipmentRepository {

    override suspend fun saveEquipment(racket: Equipment) {
        equipmentApiService.saveEquipment(racket.toDTO())
    }

    override suspend fun deleteEquipment(racketId: Int) {
        equipmentApiService.deleteEquipment(racketId)
    }

    override suspend fun getEquipmentById(racketId: Int): Equipment? {
        val uid = getCurrentUserUseCase()?.uid ?: return null
        val userProfile = authApiService.getUserById(uid) ?: return null

        // Kikeresjük az ütőt az id alapján
        val racket = userProfile.equipment.find { it.id == racketId }

        // DTO-ból Domain Modellbe mapeljük
        return racket
    }
}