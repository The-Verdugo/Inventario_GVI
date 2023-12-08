package com.grupoventa.inventario_gvi.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.grupoventa.inventario_gvi.data.database.dao.ItemsDao
import com.grupoventa.inventario_gvi.data.database.entities.InventarioInicialEntity

@Database(entities = [InventarioInicialEntity::class], version = 1)
abstract class InventarioDatabase:RoomDatabase() {
    abstract fun getItemsDao():ItemsDao
}