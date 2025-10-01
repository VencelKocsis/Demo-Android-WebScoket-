package hu.bme.aut.android.demo.data.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.network.ApiService
import hu.bme.aut.android.demo.data.network.PlayersWebSocketClient
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory // <-- GSON importálva
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Az URL a 10.0.2.2:8080/ (Android emulátor localhost-ja)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // ----------------------------------------------------
    // 1. OkHttpClient (Minden hálózati forgalomhoz: REST és WS)
    // ----------------------------------------------------
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // A ws kliens mostantól ezt a Singleton OkHttpClient-et fogja használni
        return OkHttpClient.Builder()
            // Itt adhatsz hozzá interceptorokat, timeout beállításokat stb.
            .build()
    }

    // ----------------------------------------------------
    // 2. Retrofit (REST API hívásokhoz)
    // ----------------------------------------------------
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // GSON inicializálása (a régi NetworkClient miatt)
        val gson = Gson()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            // A Singleton OkHttpClient használata
            .client(okHttpClient)

            // Konverterek hozzáadása (a régi NetworkClient alapján):
            // 1. Scalars (DELETE végpontokhoz)
            .addConverterFactory(ScalarsConverterFactory.create())
            // 2. GSON (a JSON DTO-khoz, mivel ez volt a régi kódodban)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // ----------------------------------------------------
    // 3. ApiService (A Retrofit interfész implementációja)
    // ----------------------------------------------------
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // ----------------------------------------------------
    // 4. PlayersWebSocketClient (WS kapcsolat)
    // ----------------------------------------------------
    // Ennek a kliensnek KÉT függőségre van szüksége: az OkHttpClient-re és a Json objektumra.
    @Provides
    @Singleton
    fun providePlayersWebSocketClient(okHttpClient: OkHttpClient): PlayersWebSocketClient {
        // Létrehozzuk a kotlinx.serialization Json példányt,
        // amit a WS kliens a DTO-k dekódolásához használ
        val json = Json {
            classDiscriminator = "type"
            ignoreUnknownKeys = true
        }

        // Itt át kell majd alakítanod a PlayersWebSocketClient konstruktorát!
        // A Hilt injektálja a Singleton OkHttpClient-et és a Json-t.
        // Ezt a lépést lent részletezem!
        return PlayersWebSocketClient(okHttpClient, json)
    }
}