package hu.bme.aut.android.demo.domain.market.repository

import hu.bme.aut.android.demo.domain.market.model.MarketItem

interface MarketRepository {
    suspend fun getMarketItems(): List<MarketItem>
    suspend fun inquireAboutEquipment(equipmentId: Int)
}