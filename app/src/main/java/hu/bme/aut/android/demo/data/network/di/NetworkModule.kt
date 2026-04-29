package hu.bme.aut.android.demo.data.network.di

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.network.api.* // Importáljuk a szétbontott API fájlokat
import hu.bme.aut.android.demo.data.network.api.equipment.EquipmentApiService
import hu.bme.aut.android.demo.data.network.api.equipment.EquipmentApiServiceImpl
import hu.bme.aut.android.demo.data.network.api.equipment.EquipmentRetrofitApi
import hu.bme.aut.android.demo.data.network.api.fcm.FcmApiService
import hu.bme.aut.android.demo.data.network.api.fcm.FcmApiServiceImpl
import hu.bme.aut.android.demo.data.network.api.fcm.FcmRetrofitApi
import hu.bme.aut.android.demo.data.network.api.market.MarketApiService
import hu.bme.aut.android.demo.data.network.api.market.MarketApiServiceImpl
import hu.bme.aut.android.demo.data.network.api.market.MarketRetrofitApi
import hu.bme.aut.android.demo.data.network.api.match.MatchApiService
import hu.bme.aut.android.demo.data.network.api.match.MatchApiServiceImpl
import hu.bme.aut.android.demo.data.network.api.match.MatchRetrofitApi
import hu.bme.aut.android.demo.data.network.api.team.TeamApiService
import hu.bme.aut.android.demo.data.network.api.team.TeamApiServiceImpl
import hu.bme.aut.android.demo.data.network.api.team.TeamRetrofitApi
import hu.bme.aut.android.demo.data.network.interceptor.AuthInterceptor
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Az URL a 10.0.2.2:8080/ (Android emulátor localhost-ja)
    private const val BASE_URL = "https://ktor-demo-c3yb.onrender.com/"

    @Provides
    @Singleton
    fun provideAuthInterceptor(auth: FirebaseAuth): AuthInterceptor {
        return AuthInterceptor(auth)
    }

    // ----------------------------------------------------
    // 1. OkHttpClient (Minden hálózati forgalomhoz)
    // ----------------------------------------------------
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // A Level.BODY mindent kiír: URL, Fejlécek (itt lesz a Token!) és a JSON test is.
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
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
    // 3. Json Serializer (Ezt a WS-hez használjuk, de közös beállítás lehet)
    // ----------------------------------------------------
    @Provides
    @Singleton
    fun provideJsonSerializer(): Json {
        return Json {
            classDiscriminator = "type"
            ignoreUnknownKeys = true
        }
    }

    // ----------------------------------------------------
    // 4. Specifikus Retrofit API-k biztosítása
    // ----------------------------------------------------
    @Provides
    @Singleton
    fun provideFcmRetrofitApi(retrofit: Retrofit): FcmRetrofitApi = retrofit.create(FcmRetrofitApi::class.java)

    @Provides
    @Singleton
    fun provideTeamRetrofitApi(retrofit: Retrofit): TeamRetrofitApi = retrofit.create(TeamRetrofitApi::class.java)

    @Provides
    @Singleton
    fun provideMatchRetrofitApi(retrofit: Retrofit): MatchRetrofitApi = retrofit.create(MatchRetrofitApi::class.java)

    @Provides
    @Singleton
    fun provideMarketRetrofitApi(retrofit: Retrofit): MarketRetrofitApi = retrofit.create(MarketRetrofitApi::class.java)

    @Provides
    @Singleton
    fun provideEquipmentRetrofitApi(retrofit: Retrofit): EquipmentRetrofitApi {
        return retrofit.create(EquipmentRetrofitApi::class.java)
    }

    // ----------------------------------------------------
    // 5. Interfészek és Implementációk összekötése (@Binds)
    // ----------------------------------------------------
    @Module
    @InstallIn(SingletonComponent::class)
    abstract class NetworkBindsModule {

        @Binds
        @Singleton
        abstract fun bindFcmApiService(impl: FcmApiServiceImpl): FcmApiService

        @Binds
        @Singleton
        abstract fun bindTeamApiService(impl: TeamApiServiceImpl): TeamApiService

        @Binds
        @Singleton
        abstract fun bindMatchApiService(impl: MatchApiServiceImpl): MatchApiService

        @Binds
        @Singleton
        abstract fun bindMarketApiService(impl: MarketApiServiceImpl): MarketApiService

        @Binds
        @Singleton
        abstract fun bindEquipmentApiService(impl: EquipmentApiServiceImpl): EquipmentApiService
    }
}