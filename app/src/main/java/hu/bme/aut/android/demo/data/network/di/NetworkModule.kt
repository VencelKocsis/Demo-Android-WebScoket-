package hu.bme.aut.android.demo.data.network.di

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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

/**
 * Központi Hilt modul a hálózati (Network) réteghez.
 * * Itt építjük fel a HTTP klienst (OkHttp), a JSON konvertereket és a Retrofit példányt.
 * * Ez az egyetlen hely, ahol a projekt "összedrótozza" a hálózati keretrendszereket.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://ktor-demo-c3yb.onrender.com/"

    /**
     * Biztosítja az azonosítást végző Interceptor-t.
     * Ez csatolja a Firebase tokent minden egyes kimenő HTTP kéréshez.
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(auth: FirebaseAuth): AuthInterceptor {
        return AuthInterceptor(auth)
    }

    /**
     * Felépíti az OkHttpClient-et, amely a tényleges hálózati forgalmat bonyolítja.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Minden kérést/választ kilogol a Logcatbe
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Létrehozza a Retrofit példányt, amely a hálózati hívásokat generálja az interfészek alapján.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = Gson()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create()) // Szöveges válaszokhoz
            .addConverterFactory(GsonConverterFactory.create(gson)) // JSON konverzióhoz (Gson)
            .build()
    }

    /**
     * Biztosítja a kotlinx.serialization Json példányát (főleg WebSocketekhez használatos).
     */
    @Provides
    @Singleton
    fun provideJsonSerializer(): Json {
        return Json {
            classDiscriminator = "type"
            ignoreUnknownKeys = true // Nem omlik össze, ha a szerver új, ismeretlen mezőt küld
        }
    }

    // --- ALACSONY SZINTŰ RETROFIT API-K LÉTREHOZÁSA ---

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
    fun provideEquipmentRetrofitApi(retrofit: Retrofit): EquipmentRetrofitApi = retrofit.create(EquipmentRetrofitApi::class.java)

    // --- TISZTA INTERFÉSZEK ÉS IMPLEMENTÁCIÓK ÖSSZEKÖTÉSE (A HÍD) ---

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