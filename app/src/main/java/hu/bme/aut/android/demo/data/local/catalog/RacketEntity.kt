package hu.bme.aut.android.demo.data.local.catalog

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blades")
data class BladeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val manufacturer: String,
    val model: String
)

@Entity(tableName = "rubbers")
data class RubberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val manufacturer: String,
    val model: String
)
