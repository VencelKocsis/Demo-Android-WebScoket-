package hu.bme.aut.android.demo.data.network.api.equipment

import hu.bme.aut.android.demo.data.racket.model.RacketDTO
import javax.inject.Inject

class EquipmentApiServiceImpl @Inject constructor(
    private val equipmentRetrofitApi: EquipmentRetrofitApi
) : EquipmentApiService {
    override suspend fun saveEquipment(racketDTO: RacketDTO) = equipmentRetrofitApi.saveEquipment(racketDTO)
    override suspend fun deleteEquipment(racketId: Int) = equipmentRetrofitApi.deleteEquipment(racketId)
}