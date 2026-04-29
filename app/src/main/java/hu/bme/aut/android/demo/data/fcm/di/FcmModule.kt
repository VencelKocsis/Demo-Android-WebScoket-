package hu.bme.aut.android.demo.data.fcm.di

import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt modul, amely a Firebase Cloud Messaging (FCM) szolgáltatásokhoz
 * szükséges külső függőségeket biztosítja az alkalmazás számára.
 */
@Module
@InstallIn(SingletonComponent::class)
object FcmModule {

    /**
     * Biztosítja a [FirebaseMessaging] egyetlen (Singleton) példányát a Hilt számára.
     * Ezt használjuk a kliens oldali FCM tokenek lekérésére.
     */
    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }
}