package hu.bme.aut.android.demo.data.local.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.local.catalog.CatalogDao
import hu.bme.aut.android.demo.data.local.catalog.CatalogDatabase
import hu.bme.aut.android.demo.data.local.repository.CatalogRepositoryImpl
import hu.bme.aut.android.demo.domain.catalog.repository.CatalogRepository
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CatalogDatabaseModule {

    @Provides
    @Singleton
    fun provideCatalogDatabase(
        @ApplicationContext context: Context,
        provider: Provider<CatalogDatabase>
    ): CatalogDatabase {
        return Room.databaseBuilder(
            context,
            CatalogDatabase::class.java,
            "table_tennis_catalog.db"
        )
            // Bekötjük az első indításos feltöltőt
            .addCallback(CatalogDatabase.CatalogDatabaseCallback(provider))
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCatalogDao(database: CatalogDatabase): CatalogDao {
        return database.catalogDao()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class CatalogRepositoryModule {

        @Binds
        abstract fun bindCatalogRepository(
            catalogRepositoryImpl: CatalogRepositoryImpl
        ): CatalogRepository
    }
}