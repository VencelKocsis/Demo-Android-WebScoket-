package hu.bme.aut.android.demo.domain.catalog.usecase

import hu.bme.aut.android.demo.domain.catalog.repository.CatalogRepository
import jakarta.inject.Inject

/**
 * UseCase az asztalitenisz fagyártók (Blade Manufacturers) listájának lekérdezéséhez.
 * * Jellemzően az ütőszerkesztő legördülő menüjének (Dropdown) feltöltésére használjuk.
 */
class GetBladeManufacturersUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    /**
     * @return A fagyártók listája szöveges formátumban.
     */
    suspend operator fun invoke(): List<String> {
        return repository.getBladeManufacturers()
    }
}