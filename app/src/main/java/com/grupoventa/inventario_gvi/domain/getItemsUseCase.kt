package com.grupoventa.inventario_gvi.domain

import com.grupoventa.inventario_gvi.data.ItemRepository
import com.grupoventa.inventario_gvi.data.database.entities.toDatabase
import com.grupoventa.inventario_gvi.data.model.ItemSAP
import com.grupoventa.inventario_gvi.data.model.ResponseModel
import com.grupoventa.inventario_gvi.domain.model.ItemInventario
import javax.inject.Inject


class getItemsUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke():List<ItemInventario> {
      val items = repository.getAllItemsFromApi()

      return if (items.isNotEmpty()){
          repository.clearItems()
          repository.insertItemsInicial(items.map { it.toDatabase() })
          items
      }else{
          repository.getAllItemsFromDatabase()
      }
    }
}