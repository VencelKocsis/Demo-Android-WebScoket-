package hu.bme.aut.android.demo.data.auth.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.auth.repository.AuthRepositoryImpl
import hu.bme.aut.android.demo.data.network.api.auth.AuthApiService
import hu.bme.aut.android.demo.data.network.api.auth.AuthApiServiceImpl
import hu.bme.aut.android.demo.data.network.api.auth.AuthRetrofitApi
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt modul, amely harmadik féltől származó osztályok (pl. Firebase)
 * injektálását biztosítja az alkalmazás számára, és
 * az interfészekhez rendeli a megfelelő implementációkat.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    // --- @Provides: Harmadik fél könyvtárak / Generált API-k biztosítása ---

    /**
     * Biztosítja a FirebaseAuth egyetlen példányát a Hilt számára.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    /**
     * Létrehozza az AuthRetrofitApi-t a globális Retrofit példány segítségével.
     * (A Retrofit példányt a Hilt automatikusan injektálja a NetworkModule-ból).
     */
    @Provides
    @Singleton
    fun provideAuthRetrofitApi(retrofit: Retrofit): AuthRetrofitApi {
        return retrofit.create(AuthRetrofitApi::class.java)
    }

    // --- @Binds: Interfész és implementáció összekapcsolása ---

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class AuthModuleBinds {

        /**
         * Összekapcsolja az AuthRepository interfészt az AuthRepositoryImpl implementációval.
         */
        @Binds
        @Singleton
        abstract fun bindAuthRepository(
            authRepositoryImpl: AuthRepositoryImpl
        ): AuthRepository

        /**
         * Összekapcsolja az AuthApiService interfészt az AuthApiServiceImpl implementációval.
         */
        @Binds
        @Singleton
        abstract fun bindAuthApiService(
            authApiServiceImpl: AuthApiServiceImpl
        ): AuthApiService
    }
}