package hu.bme.aut.android.demo.data.network.api.equipment

import hu.bme.aut.android.demo.domain.equipment.model.Equipment

/**
 * A felszerelések hálózati műveleteinek elvont szerződése.
 * * Elrejti a Repository elől a hálózati megvalósítást, és tisztán a Domain
 * modellel ([Equipment]) dolgozik.
 */
interface EquipmentApiService {
    suspend fun saveEquipment(equipment: Equipment)
    suspend fun deleteEquipment(racketId: Int)
}