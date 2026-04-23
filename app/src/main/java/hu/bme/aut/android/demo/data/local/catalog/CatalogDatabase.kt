package hu.bme.aut.android.demo.data.local.catalog

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

@Database(entities = [BladeEntity::class, RubberEntity::class], version = 1, exportSchema = false)
abstract class CatalogDatabase : RoomDatabase() {

    abstract fun catalogDao(): CatalogDao

    class CatalogDatabaseCallback(
        private val databaseProvider: Provider<CatalogDatabase>
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            CoroutineScope(Dispatchers.IO).launch {
                populateDatabase(databaseProvider.get().catalogDao())
            }
        }

        private suspend fun populateDatabase(dao: CatalogDao) {
            val initialBlades = listOf(
                "Butterfly" to listOf("Timo Boll ALC", "Viscaria", "Innerforce Layer ZLC"),
                "DHS" to listOf("Ma Long 5", "Hurricane Long 5", "Power G9"),
                "Yasaka" to listOf("Ma Lin Extra Offensive", "Gatien Extra"),
                "Stiga" to listOf("Offensive Classic", "Carbonado 45"),
                "Tibhar" to listOf("Samsonov Force Pro", "Stratus Powerwood")
            ).flatMap { (manufacturer, models) ->
                models.map { model -> BladeEntity(manufacturer = manufacturer, model = model) }
            }

            val initialRubbers = listOf(
                "Butterfly" to listOf("Tenergy 05", "Dignics 09c", "Rozena"),
                "DHS" to listOf("Hurricane 3 NEO", "National Hurricane 3", "Skyline 3"),
                "Yasaka" to listOf("Rakza 7 Soft", "Rakza PO"),
                "Stiga" to listOf("Calibra LT", "Mantra S"),
                "Tibhar" to listOf("Evolution MX-P", "Quantum X Pro")
            ).flatMap { (manufacturer, models) ->
                models.map { model -> RubberEntity(manufacturer = manufacturer, model = model) }
            }

            dao.insertBlades(initialBlades)
            dao.insertRubbers(initialRubbers)
        }
    }
}