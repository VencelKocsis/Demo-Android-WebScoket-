package hu.bme.aut.android.demo.data.network.api.market

import hu.bme.aut.android.demo.data.market.model.MarketItemDTO

interface MarketApiService {
    suspend fun getMarketItems(): List<MarketItemDTO>
    suspend fun inquireAboutEquipment(equipmentId: Int)
}