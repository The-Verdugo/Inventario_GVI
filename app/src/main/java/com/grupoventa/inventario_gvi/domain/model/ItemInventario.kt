package com.grupoventa.inventario_gvi.domain.model

import com.grupoventa.inventario_gvi.data.database.entities.InventarioInicialEntity
import com.grupoventa.inventario_gvi.data.model.ItemSAP

data class ItemInventario(
    val CodeBars:String? = null,
    var DistNumber: String,
    val FirmName: String,
    val ItemCode: String,
    val ItmsGrpNam: String,
    val Tipo_Conteo: Int,
    val U_CATEGORIA: String,
    val WhsCode: String,
    val cantidad: Double,
    val itemName: String
)
fun ItemSAP.toDomain() = ItemInventario(CodeBars, DistNumber, FirmName, ItemCode, ItmsGrpNam, Tipo_Conteo, U_CATEGORIA, WhsCode, cantidad, itemName)
fun InventarioInicialEntity.toDomain() = ItemInventario(CodeBars, DistNumber, FirmName, ItemCode, ItmsGrpNam, Tipo_Conteo, U_CATEGORIA, WhsCode, cantidad, itemName)