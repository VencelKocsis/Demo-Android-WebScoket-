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

/**
 * Hilt modul, ami a helyi Room adatbázis és a DAO példányosításáért,
 * valamint a Catalog Repository összekapcsolásáért felel.
 */
@Module
@InstallIn(SingletonComponent::class)
object CatalogDatabaseModule {

    /**
     * Létrehozza és biztosítja a [CatalogDatabase] egyetlen példányát.
     * Hozzáadja a [CatalogDatabase.CatalogDatabaseCallback]-et is az első induláskori adatfeltöltéshez.
     */
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
            // Ha változik az adatbázis verziója (és nincs migráció írva), törli a régit és újat hoz létre
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Biztosítja a DAO-t (Data Access Object), amin keresztül futtathatók az SQL lekérdezések.
     */
    @Provides
    @Singleton
    fun provideCatalogDao(database: CatalogDatabase): CatalogDao {
        return database.catalogDao()
    }

    /**
     * Belső modul az interfészek implementációkkal való összekötéséhez.
     */
    @Module
    @InstallIn(SingletonComponent::class)
    abstract class CatalogRepositoryModule {

        /**
         * A Domain rétegbeli [CatalogRepository]-t a Data rétegbeli [CatalogRepositoryImpl]-hez rendeli.
         */
        @Binds
        abstract fun bindCatalogRepository(
            catalogRepositoryImpl: CatalogRepositoryImpl
        ): CatalogRepository
    }
}