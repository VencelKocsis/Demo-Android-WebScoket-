package hu.bme.aut.android.demo.domain.catalog.usecase

import hu.bme.aut.android.demo.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

/**
 * UseCase az asztalitenisz borításgyártók (Rubber Manufacturers) listájának lekérdezéséhez.
 * * Az ütőszerkesztő (tenyeres vagy fonák) legördülő menüjének feltöltésére szolgál.
 */
class GetRubberManufacturersUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    /**
     * @return A borításgyártók listája szöveges formátumban.
     */
    suspend operator fun invoke(): List<String> {
        return repository.getRubberManufacturers()
    }
}