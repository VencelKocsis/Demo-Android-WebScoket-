package hu.bme.aut.android.demo.data.team.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.team.repository.TeamRepositoryImpl
import hu.bme.aut.android.demo.domain.team.repository.TeamRepository
import javax.inject.Singleton

/**
 * Hilt modul, amely a csapatokkal (Team) kapcsolatos függőség-injektálásért felel.
 * * Összeköti a tiszta Domain interfészeket a Data rétegbeli megvalósításokkal.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TeamModule {

    /**
     * Ezzel mondjuk meg a Hilt-nek:
     * "Ha valaki (pl. egy UseCase) a [TeamRepository] interfészt kéri a konstruktorában,
     * akkor példányosítsd és add oda neki a [TeamRepositoryImpl]-t!"
     */
    @Binds
    @Singleton
    abstract fun bindTeamRepository(
        teamRepositoryImpl: TeamRepositoryImpl
    ): TeamRepository
}