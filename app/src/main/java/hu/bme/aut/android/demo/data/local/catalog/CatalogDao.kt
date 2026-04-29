package hu.bme.aut.android.demo.data.local.catalog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) a helyi katalógus adatbázishoz.
 * * Felelős a táblitenisz felszerelések (fák és borítások) lekérdezéséért és beszúrásáért.
 * A Room könyvtár automatikusan generálja le a megfelelő SQL kódokat ezen interfész alapján.
 */
@Dao
interface CatalogDao {

    // --- FÁK (BLADES) ---

    /**
     * Lekérdezi az összes egyedi fagyártót abc sorrendben.
     * @return A gyártók listája (pl. "Butterfly", "DHS").
     */
    @Query("SELECT DISTINCT manufacturer FROM blades ORDER BY manufacturer ASC")
    suspend fun getBladeManufacturers(): List<String>

    /**
     * Lekérdezi egy adott gyártóhoz tartozó összes famodellt abc sorrendben.
     * @param manufacturer A kiválasztott gyártó neve.
     * @return A modellek listája.
     */
    @Query("SELECT model FROM blades WHERE manufacturer = :manufacturer ORDER BY model ASC")
    suspend fun getBladeModels(manufacturer: String): List<String>

    /**
     * Beszúrja a fák listáját az adatbázisba. Ha ütközés van, felülírja a meglévőt.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlades(blades: List<BladeEntity>)

    // --- BORÍTÁSOK (RUBBERS) ---

    /**
     * Lekérdezi az összes egyedi borításgyártót abc sorrendben.
     */
    @Query("SELECT DISTINCT manufacturer FROM rubbers ORDER BY manufacturer ASC")
    suspend fun getRubberManufacturers(): List<String>

    /**
     * Lekérdezi egy adott gyártóhoz tartozó összes borításmodellt abc sorrendben.
     */
    @Query("SELECT model FROM rubbers WHERE manufacturer = :manufacturer ORDER BY model ASC")
    suspend fun getRubberModels(manufacturer: String): List<String>

    /**
     * Beszúrja a borítások listáját az adatbázisba.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRubbers(rubbers: List<RubberEntity>)
}