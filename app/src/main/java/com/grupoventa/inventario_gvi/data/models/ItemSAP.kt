package com.grupoventa.inventario_gvi.data.models

data class ItemSAP (
    val CodeBars:String,
    val DistNumber: String,
    val FirmName: String,
    val ItemCode: String,
    val ItmsGrpNam: String,
    val Tipo_Conteo: Int,
    val U_CATEGORIA: String,
    val WhsCode: String,
    val cantidad: Double,
    val itemName: String
) {
    constructor() : this("","", "", "", "", 0, "", "", 0.0, "")
}
