package com.grupoventa.inventario_gvi.data.network

import android.util.Log
import com.grupoventa.inventario_gvi.core.RetrofitHelper
import com.grupoventa.inventario_gvi.data.model.DataSapRequest
import com.grupoventa.inventario_gvi.data.model.ItemSAP
import com.grupoventa.inventario_gvi.data.model.ResponseModel
import javax.inject.Inject

class ItemsService @Inject constructor(
    private val api: ItemsApiClient
) {
    suspend fun getItems(pagina:Int): ResponseModel {
        val dataSAPRequest = DataSapRequest(Pagina = pagina, Size = 500)
        val result: ResponseModel = api.getDataSAP(dataSAPRequest)

        Log.d("API_RESPONSE", "Response: $result")

        return result
    }
}
