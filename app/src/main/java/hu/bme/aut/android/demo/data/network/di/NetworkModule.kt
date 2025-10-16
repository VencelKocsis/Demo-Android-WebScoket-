package hu.bme.aut.android.demo.data.network.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.network.api.ApiService
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Az URL a 10.0.2.2:8080/ (Android emulátor localhost-ja)
    private const val BASE_URL = "https://ktor-demo-c3yb.onrender.com/"

    // ----------------------------------------------------
    // 1. OkHttpClient (Minden hálózati forgalomhoz)
    // ----------------------------------------------------
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    // ----------------------------------------------------
    // 2. Retrofit (REST API hívásokhoz)
    // ----------------------------------------------------
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = Gson()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
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

    // 4. Json Serializer (Ezt a WS-hez használjuk, de közös beállítás lehet)
    @Provides
    @Singleton
    fun provideJsonSerializer(): Json {
        return Json {
            classDiscriminator = "type"
            ignoreUnknownKeys = true
        }
    }
}
