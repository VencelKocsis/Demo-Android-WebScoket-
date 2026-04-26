package hu.bme.aut.android.demo.domain.equipment.repository

import hu.bme.aut.android.demo.domain.equipment.model.Equipment

interface EquipmentRepository {
    suspend fun saveEquipment(racket: Equipment)
    suspend fun deleteEquipment(racketId: Int)
    suspend fun getEquipmentById(racketId: Int): Equipment?
}