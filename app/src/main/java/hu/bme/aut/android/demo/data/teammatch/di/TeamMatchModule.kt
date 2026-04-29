package hu.bme.aut.android.demo.data.teammatch.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.domain.teammatch.repository.TeamMatchRepository
import hu.bme.aut.android.demo.data.teammatch.repository.TeamMatchRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt modul, amely a mérkőzésekkel (TeamMatch) kapcsolatos függőség-injektálásért felel.
 * * A Hilt a fordítás során automatikusan feldolgozza ezt a modult.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TeamMatchModule {

    /**
     * Megmondja a Hiltnek, hogy amikor egy osztály (pl. egy UseCase) a tiszta
     * [TeamMatchRepository] interfészt kéri, akkor a [TeamMatchRepositoryImpl]
     * példányt kell biztosítania számára.
     */
    @Binds
    @Singleton
    abstract fun bindTeamMatchRepository(
        teamMatchRepositoryImpl: TeamMatchRepositoryImpl
    ): TeamMatchRepository
}