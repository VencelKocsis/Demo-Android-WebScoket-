package hu.bme.aut.android.demo.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.repository.PlayerRepositoryImpl
import hu.bme.aut.android.demo.domain.repository.PlayerRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(
        playerRepositoryImpl: PlayerRepositoryImpl
    ): PlayerRepository
}