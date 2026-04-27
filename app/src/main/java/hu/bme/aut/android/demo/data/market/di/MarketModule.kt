package hu.bme.aut.android.demo.data.market.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.market.repository.MarketRepositoryImpl
import hu.bme.aut.android.demo.domain.market.repository.MarketRepository
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MarketModule {

    @Binds
    @Singleton
    abstract fun bindMarketRepository(
        marketRepositoryImpl: MarketRepositoryImpl
    ): MarketRepository
}