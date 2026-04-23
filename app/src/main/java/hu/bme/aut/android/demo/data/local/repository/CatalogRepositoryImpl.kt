package hu.bme.aut.android.demo.data.local.repository

import hu.bme.aut.android.demo.data.local.catalog.CatalogDao
import hu.bme.aut.android.demo.domain.catalog.repository.CatalogRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton

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