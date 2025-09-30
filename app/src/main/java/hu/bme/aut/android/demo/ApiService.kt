package hu.bme.aut.android.demo

import hu.bme.aut.android.demo.models.NewPlayerDTO
import hu.bme.aut.android.demo.models.PlayerDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("players")
    suspend fun getPlayers(): List<PlayerDTO>

    @POST("players")
    suspend fun addPlayer(@Body player: NewPlayerDTO): PlayerDTO

    @DELETE("players/{id}")
    suspend fun deletePlayer(@Path("id") id: Int)
}
