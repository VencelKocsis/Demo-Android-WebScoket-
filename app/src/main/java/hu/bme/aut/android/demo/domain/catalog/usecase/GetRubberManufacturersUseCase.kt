package hu.bme.aut.android.demo.domain.catalog.usecase

import hu.bme.aut.android.demo.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

class GetRubberManufacturersUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): List<String> {
        return repository.getRubberManufacturers()
    }
}