package hu.bme.aut.android.demo.data.websocket.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.fcm.repository.FcmRepositoryImpl
import hu.bme.aut.android.demo.data.websocket.repository.MatchWsRepositoryImpl
import hu.bme.aut.android.demo.domain.fcm.repository.FcmRepository
import hu.bme.aut.android.demo.domain.websocket.repository.MatchWsRepository
import javax.inject.Singleton

/**
 * Hilt modul a Repository interfészek implementációinak biztosításához.
 * (Megjegyzés: Az FCM repository kötése technikailag egy FcmModule-ban is lehetne,
 * de egy közös RepositoryModule is teljesen elfogadott megoldás.)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMatchRepository(
        matchRepositoryImpl: MatchWsRepositoryImpl
    ): MatchWsRepository

    @Binds
    @Singleton
    abstract fun bindFcmRepository(
        fcmRepositoryImpl: FcmRepositoryImpl
    ): FcmRepository
}