package hu.bme.aut.android.demo.data.network.api.market

import hu.bme.aut.android.demo.data.market.model.MarketItemDTO
import javax.inject.Inject

class MarketApiServiceImpl @Inject constructor(
    private val marketRetrofitApi: MarketRetrofitApi
) : MarketApiService {
    override suspend fun getMarketItems(): List<MarketItemDTO> = marketRetrofitApi.getMarketItems()
    override suspend fun inquireAboutEquipment(equipmentId: Int) = marketRetrofitApi.inquireAboutEquipment(equipmentId)
}