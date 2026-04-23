package hu.bme.aut.android.demo.domain.catalog.usecase

import hu.bme.aut.android.demo.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class GetBladeModelsUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(manufacturer: String): List<String> {
        return repository.getBladeModels(manufacturer)
    }
}