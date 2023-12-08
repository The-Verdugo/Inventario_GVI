package com.grupoventa.inventario_gvi.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grupoventa.inventario_gvi.data.model.ItemSAP

import com.grupoventa.inventario_gvi.domain.GetItemIfExistUseCase
import com.grupoventa.inventario_gvi.domain.getItemsUseCase
import com.grupoventa.inventario_gvi.domain.model.ItemInventario
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor(
    private val getItemsUseCase: getItemsUseCase,
    private val findItem: GetItemIfExistUseCase
): ViewModel() {
    val inventarioModel = MutableLiveData<List<ItemInventario>?>()
    val Item = MutableLiveData<ItemInventario?>()
    val isLoading = MutableLiveData<Boolean>()

    fun onCreate() {
        viewModelScope.launch {
            isLoading.postValue(true)
            val result = getItemsUseCase()
            if(result.isNotEmpty()){
                inventarioModel.postValue(result)
                isLoading.postValue(false)
            }
        }
    }

    fun FindItemSKULOT(findTerm: String, Lot: String, Type: String) {
        viewModelScope.launch {
            val item: ItemInventario? = findItem(findTerm, Lot, Type)
            if (item != null) {
                val modifiedItem = if (Type == "SkuOrCod") {
                    item.copy(DistNumber = "N/A")
                } else {
                    item
                }
                Item.postValue(modifiedItem)
            } else {
                Item.postValue(null)
            }
        }
    }
}