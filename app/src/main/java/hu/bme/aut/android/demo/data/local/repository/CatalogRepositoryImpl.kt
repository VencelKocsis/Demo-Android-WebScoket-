package hu.bme.aut.android.demo.data.local.repository

import hu.bme.aut.android.demo.data.local.catalog.CatalogDao
import hu.bme.aut.android.demo.domain.catalog.repository.CatalogRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * A [CatalogRepository] interfész megvalósítása a Data rétegben.
 * * Közvetítő szerepet tölt be a UseCase-ek és a helyi Room adatbázis (DAO) között.
 * Mivel itt a Domain modellek (Stringek listája) teljesen megegyeznek a DAO válaszaival,
 * nincs szükség különálló Mapper fájlokra, egyszerűen továbbítjuk a lekérdezéseket.
 */
@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val catalogDao: CatalogDao
) : CatalogRepository {

    override suspend fun getBladeManufacturers(): List<String> {
        return catalogDao.getBladeManufacturers()
    }

    override suspend fun getBladeModels(manufacturer: String): List<String> {
        return catalogDao.getBladeModels(manufacturer)
    }

    override suspend fun getRubberManufacturers(): List<String> {
        return catalogDao.getRubberManufacturers()
    }

    override suspend fun getRubberModels(manufacturer: String): List<String> {
        return catalogDao.getRubberModels(manufacturer)
    }
}