package com.grupoventa.inventario_gvi.data.database.entities

import androidx.compose.foundation.layout.Box
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grupoventa.inventario_gvi.domain.model.ItemInventario

@Entity(tableName = "inventario_inicial_table")
data class InventarioInicialEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int = 0,
    @ColumnInfo(name = "CodeBars")
    val CodeBars:String? = null,
    @ColumnInfo(name = "DistNumber")
    var DistNumber: String,
    @ColumnInfo(name = "FirmName")
    val FirmName: String,
    @ColumnInfo(name = "ItemCode")
    val ItemCode: String,
    @ColumnInfo(name = "ItmsGrpNam")
    val ItmsGrpNam: String,
    @ColumnInfo(name = "Tipo_Conteo")
    val Tipo_Conteo: Int,
    @ColumnInfo(name = "U_CATEGORIA")
    val U_CATEGORIA: String,
    @ColumnInfo(name = "WhsCode")
    val WhsCode: String,
    @ColumnInfo(name = "cantidad")
    val cantidad: Double,
    @ColumnInfo(name = "itemName")
    val itemName: String
)

fun ItemInventario.toDatabase() = InventarioInicialEntity(
    CodeBars = CodeBars,
    DistNumber = DistNumber,
    FirmName = FirmName,
    ItemCode = ItemCode,
    ItmsGrpNam = ItmsGrpNam,
    Tipo_Conteo = Tipo_Conteo,
    U_CATEGORIA = U_CATEGORIA,
    WhsCode = WhsCode,
    cantidad = cantidad,
    itemName = itemName
)