package com.grupoventa.inventario_gvi.data

import android.util.Log
import com.grupoventa.inventario_gvi.data.database.dao.ItemsDao
import com.grupoventa.inventario_gvi.data.database.entities.InventarioInicialEntity
import com.grupoventa.inventario_gvi.data.model.ResponseModel
import com.grupoventa.inventario_gvi.data.network.ItemsService
import com.grupoventa.inventario_gvi.domain.model.ItemInventario
import com.grupoventa.inventario_gvi.domain.model.toDomain
import javax.inject.Inject

class ItemRepository @Inject constructor(
    private val api:ItemsService,
    private val ItemsDao: ItemsDao
) {
    suspend fun getAllItemsFromApi(): List<ItemInventario> {
        var currentPage = 1
        val items = mutableListOf<ItemInventario>()

        do {
            val response: ResponseModel = api.getItems(currentPage)

            // Agregar los elementos de la página actual a la lista
            items.addAll(response.Data.map { it.toDomain() })

            // Incrementar el número de página para la próxima iteración
            currentPage++

        } while (currentPage <= response.TotalPages)


        // Imprimir información sobre la respuesta (puedes quitar esto en producción)
        Log.d("API_RESPONSE", "Items: ${items.size}")

        return items
    }

    suspend fun getAllItemsFromDatabase():List<ItemInventario>{
        val response = ItemsDao.getAllInitialItems()
        return response.map { it.toDomain() }
    }

    suspend fun insertItemsInicial(Items:List<InventarioInicialEntity>){
        ItemsDao.insertAll(Items)
    }

    suspend fun clearItems(){
        ItemsDao.deleteAllItems()
    }
}