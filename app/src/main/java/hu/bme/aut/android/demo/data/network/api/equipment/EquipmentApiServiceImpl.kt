package hu.bme.aut.android.demo.data.network.api.equipment

import hu.bme.aut.android.demo.data.equipment.mapper.toDTO
import hu.bme.aut.android.demo.domain.equipment.model.Equipment
import javax.inject.Inject

/**
 * Az [EquipmentApiService] konkrét megvalósítása.
 * * Hídként működik: a Repository-tól kapott tiszta [Equipment] modellt átalakítja
 * hálózati [RacketDTO]-vá, majd átadja a Retrofit kliensnek.
 */
class EquipmentApiServiceImpl @Inject constructor(
    private val equipmentRetrofitApi: EquipmentRetrofitApi
) : EquipmentApiService {
    override suspend fun saveEquipment(equipment: Equipment) =
        equipmentRetrofitApi.saveEquipment(equipment.toDTO())

    override suspend fun deleteEquipment(racketId: Int) =
        equipmentRetrofitApi.deleteEquipment(racketId)
}