package hu.bme.aut.android.demo.data.racket.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.demo.data.racket.repository.EquipmentRepositoryImpl
import hu.bme.aut.android.demo.domain.equipment.repository.EquipmentRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class EquipmentModule {

    @Binds
    abstract fun bindEquipmentRepository(
        impl: EquipmentRepositoryImpl
    ): EquipmentRepository
}