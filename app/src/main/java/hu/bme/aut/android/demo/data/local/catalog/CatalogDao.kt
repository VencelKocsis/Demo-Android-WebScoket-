package hu.bme.aut.android.demo.data.local.catalog

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CatalogDao {
    // --- FÁK (BLADES) ---
    @Query("SELECT DISTINCT manufacturer FROM blades ORDER BY manufacturer ASC")
    suspend fun getBladeManufacturers(): List<String>

    @Query("SELECT model FROM blades WHERE manufacturer = :manufacturer ORDER BY model ASC")
    suspend fun getBladeModels(manufacturer: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlades(blades: List<BladeEntity>)

    // --- BORÍTÁSOK (RUBBERS) ---
    @Query("SELECT DISTINCT manufacturer FROM rubbers ORDER BY manufacturer ASC")
    suspend fun getRubberManufacturers(): List<String>

    @Query("SELECT model FROM rubbers WHERE manufacturer = :manufacturer ORDER BY model ASC")
    suspend fun getRubberModels(manufacturer: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRubbers(rubbers: List<RubberEntity>)
}