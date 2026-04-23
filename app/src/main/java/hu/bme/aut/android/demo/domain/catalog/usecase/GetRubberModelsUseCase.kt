package hu.bme.aut.android.demo.domain.catalog.usecase

import hu.bme.aut.android.demo.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class GetRubberModelsUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(manufacturer: String): List<String> {
        return repository.getRubberModels(manufacturer)
    }
}