package com.grupoventa.inventario_gvi.domain
import com.grupoventa.inventario_gvi.data.ItemRepository
import com.grupoventa.inventario_gvi.data.model.ItemSAP
import com.grupoventa.inventario_gvi.domain.model.ItemInventario
import javax.inject.Inject

class GetItemIfExistUseCase @Inject constructor(
    private val repository: ItemRepository
) {
    suspend operator fun invoke(searchTerm: String,lot:String, type:String): ItemInventario? {
        val items = repository.getAllItemsFromDatabase()
        return when(type){
            "SkuOrCod" ->{
                items.firstOrNull {
                    it.ItemCode.equals(searchTerm, ignoreCase = true) || it.CodeBars.equals(searchTerm, ignoreCase = true)
                }
            }
            "SkuAndLot"->{
                items.firstOrNull{
                    it.ItemCode.equals(searchTerm, ignoreCase = true) && it.DistNumber.equals(lot,ignoreCase = true)
                }
            }
            "Ser"->{
                items.firstOrNull{
                    it.DistNumber.equals(searchTerm,ignoreCase = true)
                }
            }
            else -> {
                null
            }
        }
    }
}