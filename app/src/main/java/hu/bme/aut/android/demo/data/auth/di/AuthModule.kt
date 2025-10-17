package hu.bme.aut.android.demo.data.auth.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.auth.repository.AuthRepositoryImpl
import hu.bme.aut.android.demo.domain.auth.repository.AuthRepository
import javax.inject.Singleton

/**
 * Hilt modul, amely harmadik féltől származó osztályok (pl. Firebase)
 * injektálását biztosítja az alkalmazás számára, és
 * az interfészekhez rendeli a megfelelő implementációkat.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    // --- @Provides: Harmadik fél könyvtárak példányainak biztosítása ---

    /**
     * Biztosítja a FirebaseAuth egyetlen példányát a Hilt számára.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // --- @Binds: Interfész és implementáció összekapcsolása ---

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class AuthModuleBinds {

        /**
         * Összekapcsolja az AuthRepository interfészt az AuthRepositoryImpl implementációval.
         *
         * @param authRepositoryImpl Az AuthRepositoryImpl, amit a Hilt képes felépíteni
         * (mivel a FirebaseAuth-ot most már tudja biztosítani).
         * @return Az AuthRepository interfész implementációja.
         */
        @Binds
        @Singleton
        abstract fun bindAuthRepository(
            authRepositoryImpl: AuthRepositoryImpl
        ): AuthRepository
    }
}
