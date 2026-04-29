package hu.bme.aut.android.demo.data.market.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.market.repository.MarketRepositoryImpl
import hu.bme.aut.android.demo.domain.market.repository.MarketRepository
import jakarta.inject.Singleton

/**
 * Hilt modul, amely a Piac (Market) funkcióhoz kapcsolódó függőségek injektálásáért felelős.
 * * Összeköti a Domain rétegben definiált interfészeket a Data rétegben található implementációkkal.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MarketModule {

    /**
     * A [MarketRepository] interfészt a [MarketRepositoryImpl] implementációhoz köti.
     * Ennek köszönhetően a UseCase-ek a tiszta interfészt kapják meg (injektálva),
     * miközben a háttérben az API-t hívó implementáció dolgozik.
     */
    @Binds
    @Singleton
    abstract fun bindMarketRepository(
        marketRepositoryImpl: MarketRepositoryImpl
    ): MarketRepository
}