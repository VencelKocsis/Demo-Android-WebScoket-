package hu.bme.aut.android.demo.domain.catalog.repository

/**
 * A felszerelés-katalógus műveleteinek szerződése (interfésze) a Domain rétegben.
 * * Meghatározza azokat a függvényeket, amelyek az asztalitenisz fák (blades) és
 * borítások (rubbers) adatainak lekérdezéséhez szükségesek.
 * A felület (UI) és a UseCase-ek ezen keresztül kommunikálnak az adatokkal anélkül,
 * hogy tudnák, azok egy helyi Room adatbázisból származnak.
 */
interface CatalogRepository {

    /**
     * @return A rendszerben elérhető fagyártók (pl. Butterfly, Stiga) egyedi listája abc sorrendben.
     */
    suspend fun getBladeManufacturers(): List<String>

    /**
     * @param manufacturer A kiválasztott fagyártó neve.
     * @return Az adott gyártóhoz tartozó famodellek listája abc sorrendben.
     */
    suspend fun getBladeModels(manufacturer: String): List<String>

    /**
     * @return A rendszerben elérhető borításgyártók (pl. DHS, Yasaka) egyedi listája abc sorrendben.
     */
    suspend fun getRubberManufacturers(): List<String>

    /**
     * @param manufacturer A kiválasztott borításgyártó neve.
     * @return Az adott gyártóhoz tartozó borításmodellek listája abc sorrendben.
     */
    suspend fun getRubberModels(manufacturer: String): List<String>
}