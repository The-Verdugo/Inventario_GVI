package com.grupoventa.inventario_gvi.data.model

data class ResponseModel(
    val TotalRecords: Int,
    val PageSize: Int,
    val CurrentPage: Int,
    val TotalPages: Int,
    val Data: List<ItemSAP>
)