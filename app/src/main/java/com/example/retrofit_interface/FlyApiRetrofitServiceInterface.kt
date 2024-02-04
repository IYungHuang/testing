package com.example.retrofit_interface

import com.example.model.FlyDetailData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FlyApiRetrofitServiceInterface {
    @GET("AirPortFlyAPI/{flyType}/{airPortID}")
    fun getFlyData(@Path("flyType") flyType:String, @Path("airPortID") airPortID:String): Call<List<FlyDetailData>>
}