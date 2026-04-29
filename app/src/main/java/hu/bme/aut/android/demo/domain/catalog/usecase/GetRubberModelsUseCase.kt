package hu.bme.aut.android.demo.domain.catalog.usecase

import hu.bme.aut.android.demo.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

/**
 * UseCase egy kiválasztott borításgyártóhoz (Rubber) tartozó modellek lekérdezéséhez.
 * * Hasonlóan a fákhoz, ez a UseCase is dinamikusan tölti be a modelleket,
 * miután a felhasználó kiválasztott egy konkrét gyártót a tenyeres vagy fonák oldalon.
 */
class GetRubberModelsUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    /**
     * @param manufacturer A kiválasztott gyártó neve (pl. "DHS").
     * @return A gyártóhoz tartozó modellek listája (pl. ["Hurricane 3 NEO"]).
     */
    suspend operator fun invoke(manufacturer: String): List<String> {
        return repository.getRubberModels(manufacturer)
    }
}