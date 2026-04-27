package hu.bme.aut.android.demo.data.market.repository

import hu.bme.aut.android.demo.data.market.mapper.toDomain
import hu.bme.aut.android.demo.data.network.api.ApiService
import hu.bme.aut.android.demo.domain.market.model.MarketItem
import hu.bme.aut.android.demo.domain.market.repository.MarketRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : MarketRepository {

    override suspend fun getMarketItems(): List<MarketItem> {
        return apiService.getMarketItems().map { it.toDomain() }
    }

    override suspend fun inquireAboutEquipment(equipmentId: Int) {
        apiService.inquireAboutEquipment(equipmentId)
    }
}