package com.grupoventa.inventario_gvi.data.network

import com.grupoventa.inventario_gvi.data.model.DataSapRequest
import com.grupoventa.inventario_gvi.data.model.ResponseModel
import retrofit2.http.Body
import retrofit2.http.POST

interface ItemsApiClient{
    @POST("GetDataSAP")
    suspend fun getDataSAP(@Body request: DataSapRequest): ResponseModel
}
