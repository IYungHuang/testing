package com.example.base

import com.example.retrofit_interface.CurrencyApiRetrofitServiceInterface
import com.example.retrofit_interface.FlyApiRetrofitServiceInterface
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConnectManager {
    private val retrofitFly:Retrofit
    private val retrofitCurrency:Retrofit
    private val okHttpClient = OkHttpClient()

    init {
        retrofitFly = Retrofit.Builder()
            .baseUrl(FlyTPEConstants.FLY_API)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient).build()
        //
        retrofitCurrency = Retrofit.Builder()
            .baseUrl(FlyTPEConstants.CURRENCY_API)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient).build()
    }

    companion object{
        private val apiConnectManager = ApiConnectManager()
        private val clientFly:Retrofit get() = apiConnectManager.retrofitFly
        private val clientCurrency:Retrofit get() = apiConnectManager.retrofitCurrency
        val apiServiceFly:FlyApiRetrofitServiceInterface by lazy {
            clientFly.create(FlyApiRetrofitServiceInterface::class.java)
        }
        val apiServiceCurrency:CurrencyApiRetrofitServiceInterface by lazy {
            clientCurrency.create(CurrencyApiRetrofitServiceInterface::class.java)
        }
    }
}