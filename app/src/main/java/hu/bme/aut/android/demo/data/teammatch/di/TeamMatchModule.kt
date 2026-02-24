package hu.bme.aut.android.demo.data.teammatch.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.teammatch.repository.TeamMatchRepository
import hu.bme.aut.android.demo.data.teammatch.repository.TeamMatchRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TeamMatchModule {

    @Binds
    @Singleton
    abstract fun bindTeamMatchRepository(
        teamMatchRepositoryImpl: TeamMatchRepositoryImpl
    ): TeamMatchRepository
}