package hu.bme.aut.android.demo.data.network.api.equipment

import hu.bme.aut.android.demo.data.racket.model.RacketDTO

interface EquipmentApiService {
    suspend fun saveEquipment(racketDTO: RacketDTO)
    suspend fun deleteEquipment(racketId: Int)
}