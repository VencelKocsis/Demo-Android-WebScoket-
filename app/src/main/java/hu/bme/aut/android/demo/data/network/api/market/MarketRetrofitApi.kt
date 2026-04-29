package hu.bme.aut.android.demo.data.network.api.market

import hu.bme.aut.android.demo.data.market.model.MarketItemDTO
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MarketRetrofitApi {
    // Piac lekérdezése
    @GET("/api/market/equipment")
    suspend fun getMarketItems(): List<MarketItemDTO>

    // Érdeklődés küldése (Push értesítés)
    @POST("/api/market/equipment/{id}/inquire")
    suspend fun inquireAboutEquipment(@Path("id") equipmentId: Int)
}