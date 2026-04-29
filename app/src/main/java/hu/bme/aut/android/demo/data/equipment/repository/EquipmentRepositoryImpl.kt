package hu.bme.aut.android.demo.data.equipment.repository

import hu.bme.aut.android.demo.data.network.api.auth.AuthApiService
import hu.bme.aut.android.demo.data.network.api.equipment.EquipmentApiService
import hu.bme.aut.android.demo.domain.auth.usecases.GetCurrentUserUseCase
import hu.bme.aut.android.demo.domain.equipment.model.Equipment
import hu.bme.aut.android.demo.domain.equipment.repository.EquipmentRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Az [EquipmentRepository] interfész konkrét megvalósítása a Data rétegben.
 * * Közvetít a hálózati API-k ([EquipmentApiService], [AuthApiService]) és az üzleti logika között.
 */
@Singleton
class EquipmentRepositoryImpl @Inject constructor(
    private val equipmentApiService: EquipmentApiService,
    private val authApiService: AuthApiService,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : EquipmentRepository {

    override suspend fun saveEquipment(racket: Equipment) {
        equipmentApiService.saveEquipment(racket)
    }

    override suspend fun deleteEquipment(racketId: Int) {
        equipmentApiService.deleteEquipment(racketId)
    }

    override suspend fun getEquipmentById(racketId: Int): Equipment? {
        val uid = getCurrentUserUseCase()?.uid ?: return null

        // Mivel az AuthApiService már a tiszta User domaint adja vissza,
        // a benne lévő felszerelés lista már eleve Equipment (Domain) típusú!
        val userProfile = authApiService.getUserById(uid) ?: return null

        // Kikeresjük az ütőt az id alapján és azonnal vissza is adjuk (nincs szükség mappingre)
        return userProfile.equipment.find { it.id == racketId }
    }
}