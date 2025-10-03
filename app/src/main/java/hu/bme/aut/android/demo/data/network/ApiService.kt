package hu.bme.aut.android.demo.data.network

import hu.bme.aut.android.demo.domain.model.NewPlayerDTO
import hu.bme.aut.android.demo.domain.model.PlayerDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("players")
    suspend fun getPlayers(): List<PlayerDTO>

    @POST("players")
    suspend fun addPlayer(@Body player: NewPlayerDTO): PlayerDTO

    @DELETE("players/{id}")
    suspend fun deletePlayer(@Path("id") id: Int)

    @PUT("players/{id}")
    suspend fun updatePlayer(@Path("id") id: Int, @Body player: NewPlayerDTO)
}