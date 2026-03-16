package hu.bme.aut.android.demo.data.websocket.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.websocket.repository.MatchWsRepository
import hu.bme.aut.android.demo.domain.websocket.repository.MatchWsRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMatchRepository(
        matchRepositoryImpl: MatchWsRepositoryImpl
    ): MatchWsRepository
}
