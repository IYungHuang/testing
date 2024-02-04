package com.example.viewmoel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.retrofit_interface.CurrencyApiRetrofitServiceInterface
import com.example.retrofit_interface.FlyApiRetrofitServiceInterface

class AnyViewModelFactory(
    private val flyApiRetrofitServiceInterface: FlyApiRetrofitServiceInterface,
    private val currencyApiRetrofitServiceInterface: CurrencyApiRetrofitServiceInterface
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
            flyApiRetrofitServiceInterface,currencyApiRetrofitServiceInterface
        ) as T
    }
}