package hu.bme.aut.android.demo.data.racket.repository

import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.data.racket.mapper.toDTO
import hu.bme.aut.android.demo.data.racket.model.RacketDTO
import hu.bme.aut.android.demo.domain.equipment.model.Equipment
import hu.bme.aut.android.demo.domain.equipment.repository.EquipmentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : EquipmentRepository {

    override suspend fun saveEquipment(racket: Equipment) {
        apiService.saveEquipment(racket.toDTO())
    }

    override suspend fun deleteEquipment(racketId: Int) {
        apiService.deleteEquipment(racketId)
    }

}