package com.example.retrofit_interface

import com.example.model.CurrencyData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiRetrofitServiceInterface {
    @GET("latest?")
    fun getCurrencyData(@Query("apikey") apikey:String): Call<CurrencyData>
}