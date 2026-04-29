package hu.bme.aut.android.demo.data.local.catalog

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A "blades" (fák) tábla sémája a Room adatbázisban.
 *
 * @property id Automatikusan generált elsődleges kulcs.
 * @property manufacturer A fa gyártója (pl. "Butterfly").
 * @property model A fa pontos modellje (pl. "Viscaria").
 */
@Entity(tableName = "blades")
data class BladeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val manufacturer: String,
    val model: String
)

/**
 * A "rubbers" (borítások) tábla sémája a Room adatbázisban.
 *
 * @property id Automatikusan generált elsődleges kulcs.
 * @property manufacturer A borítás gyártója (pl. "DHS").
 * @property model A borítás pontos modellje (pl. "Hurricane 3 NEO").
 */
@Entity(tableName = "rubbers")
data class RubberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val manufacturer: String,
    val model: String
)