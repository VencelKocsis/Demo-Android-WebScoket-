package hu.bme.aut.android.demo.data.market.repository

import hu.bme.aut.android.demo.data.market.mapper.toDomain
import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.domain.market.model.MarketItem
import hu.bme.aut.android.demo.domain.market.repository.MarketRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A [MarketRepository] interfész megvalósítása a Data rétegben.
 * * Feladata a piaccal kapcsolatos hálózati hívások kezelése a Retrofit [ApiService]-en keresztül,
 * valamint a hálózati DTO-k lefordítása a UI számára emészthető Domain modellekre.
 */
@Singleton
class MarketRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : MarketRepository {

    /**
     * Lekérdezi a piacon (Market) jelenleg elérhető (eladásra kínált) összes felszerelést.
     * A hálózatból beérkező DTO listát azonnal Domain [MarketItem] listává alakítja (Map).
     *
     * @return Az eladó ütők és tulajdonosok listája.
     */
    override suspend fun getMarketItems(): List<MarketItem> {
        return apiService.getMarketItems().map { it.toDomain() }
    }

    /**
     * Elküld egy érdeklődést (általában Push értesítés formájában) egy adott felszerelés tulajdonosának.
     *
     * @param equipmentId Az érdeklődés tárgyát képező felszerelés egyedi azonosítója.
     */
    override suspend fun inquireAboutEquipment(equipmentId: Int) {
        apiService.inquireAboutEquipment(equipmentId)
    }
}