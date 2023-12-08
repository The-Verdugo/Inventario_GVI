package com.grupoventa.inventario_gvi.di

import android.content.Context
import androidx.room.Room
import com.grupoventa.inventario_gvi.data.database.InventarioDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object roomModule {
    const val INVENTARIO_DATABASE_NAME = "inventario_database"
    @Singleton
    @Provides
    fun provideRoom(@ApplicationContext context: Context) = Room.databaseBuilder(context,InventarioDatabase::class.java, INVENTARIO_DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideItemsDao(db:InventarioDatabase) = db.getItemsDao()
}