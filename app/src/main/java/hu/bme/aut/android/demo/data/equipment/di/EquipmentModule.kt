package hu.bme.aut.android.demo.data.equipment.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.equipment.repository.EquipmentRepositoryImpl
import hu.bme.aut.android.demo.domain.equipment.repository.EquipmentRepository

/**
 * Hilt modul a felszerelésekkel (Equipment) kapcsolatos függőségek injektálásához.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class EquipmentModule {

    /**
     * Összeköti a tiszta [EquipmentRepository] interfészt az adatrétegben
     * található [EquipmentRepositoryImpl] megvalósítással.
     */
    @Binds
    abstract fun bindEquipmentRepository(
        impl: EquipmentRepositoryImpl
    ): EquipmentRepository
}