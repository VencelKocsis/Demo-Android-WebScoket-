package hu.bme.aut.android.demo.domain.catalog.usecase

import hu.bme.aut.android.demo.domain.catalog.repository.CatalogRepository
import javax.inject.Inject

/**
 * UseCase egy kiválasztott fagyártóhoz (Blade) tartozó modellek lekérdezéséhez.
 * * Akkor hívódik meg, amikor a felhasználó kiválaszt egy gyártót, és be kell tölteni
 * az ahhoz tartozó specifikus famodelleket a következő legördülő menübe.
 */
class GetBladeModelsUseCase @Inject constructor(
    private val repository: CatalogRepository
) {
    /**
     * @param manufacturer A kiválasztott gyártó neve (pl. "Butterfly").
     * @return A gyártóhoz tartozó modellek listája (pl. ["Timo Boll ALC", "Viscaria"]).
     */
    suspend operator fun invoke(manufacturer: String): List<String> {
        return repository.getBladeModels(manufacturer)
    }
}