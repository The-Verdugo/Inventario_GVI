package com.grupoventa.inventario_gvi.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.grupoventa.inventario_gvi.data.database.entities.InventarioInicialEntity

@Dao
interface ItemsDao {
    @Query("SELECT * FROM inventario_inicial_table ORDER BY id")
    suspend fun getAllInitialItems():List<InventarioInicialEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items:List<InventarioInicialEntity>)

    @Query("DELETE FROM inventario_inicial_table")
    suspend fun deleteAllItems()

}