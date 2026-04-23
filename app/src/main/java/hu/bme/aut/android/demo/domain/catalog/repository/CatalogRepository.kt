package hu.bme.aut.android.demo.domain.catalog.repository


interface CatalogRepository {
    suspend fun getBladeManufacturers(): List<String>
    suspend fun getBladeModels(manufacturer: String): List<String>
    suspend fun getRubberManufacturers(): List<String>
    suspend fun getRubberModels(manufacturer: String): List<String>
}