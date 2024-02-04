package com.example.viewmoel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.base.FlyTPEConstants
import com.example.model.CurrencyData
import com.example.model.FlyDetailData
import com.example.retrofit_interface.CurrencyApiRetrofitServiceInterface
import com.example.retrofit_interface.FlyApiRetrofitServiceInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(
    private val flyApiRetrofitServiceInterface : FlyApiRetrofitServiceInterface,
    private val currencyApiRetrofitServiceInterface: CurrencyApiRetrofitServiceInterface
    ):ViewModel() {

    private val mRawFlyDataTakeOff = MutableLiveData<List<FlyDetailData>>()
    val rawFlyDataTakeOff : LiveData<List<FlyDetailData>> get()= mRawFlyDataTakeOff
    private val mRawFlyDataTakeArrival = MutableLiveData<List<FlyDetailData>>()
    val rawFlyDataArrival : LiveData<List<FlyDetailData>> get()= mRawFlyDataTakeArrival
    private val mRetrofitResponseErrorFly = MutableLiveData<String>()
    val retrofitErrorFly : LiveData<String> get() = mRetrofitResponseErrorFly
    //
    private val mRawCurrencyData = MutableLiveData<CurrencyData>()
    val rawCurrencyData : LiveData<CurrencyData> get()= mRawCurrencyData
    private val mRetrofitResponseErrorCurrency = MutableLiveData<String>()
    val retrofitErrorCurrency : LiveData<String> get() = mRetrofitResponseErrorCurrency

    //
    private val mInputValue = MutableLiveData("0")
    val inputValue : LiveData<String> get() = mInputValue

    private val mIsNightMode = MutableLiveData(false)
    val isNightMode : LiveData<Boolean> get() = mIsNightMode

    private val mDefaultCoin = MutableLiveData("")
    val defaultCoin : LiveData<String> get() = mDefaultCoin

    fun getFlyData(flyType:String,airPort:String) {
        val call = flyApiRetrofitServiceInterface.getFlyData(flyType,airPort)

        call.enqueue(
            object : Callback<List<FlyDetailData>> {
                override fun onResponse(call: Call<List<FlyDetailData>>, response: Response<List<FlyDetailData>>) {
                    if (response.isSuccessful) {
                        val dataBody = response.body()
                        if(flyType == "D"){
                            mRawFlyDataTakeOff.value = dataBody!!
                        }else{
                            mRawFlyDataTakeArrival.value = dataBody!!
                        }
                    } else {
                        mRetrofitResponseErrorFly.value = response.raw().code.toString()+":"+flyType
                    }
                }

                override fun onFailure(call: Call<List<FlyDetailData>>, t: Throwable) {
                    mRetrofitResponseErrorFly.value = t.toString()
                }
            }
        )
    }

    fun getCurrencyData() {
        val call = currencyApiRetrofitServiceInterface.getCurrencyData(FlyTPEConstants.API_KEY)

        call.enqueue(
            object : Callback<CurrencyData> {
                override fun onResponse(call: Call<CurrencyData>, response: Response<CurrencyData>) {
                    if (response.isSuccessful) {
                        val dataBody = response.body()
                        mRawCurrencyData.value = dataBody!!
                    } else {
                        mRetrofitResponseErrorCurrency.value = response.raw().code.toString()
                    }
                }

                override fun onFailure(call: Call<CurrencyData>, t: Throwable) {
                    mRetrofitResponseErrorCurrency.value = t.toString()
                }
            }
        )
    }

    fun setNightMode(set:Boolean){
        mIsNightMode.value = set
    }

    fun setDefaultCoin(set:String){
        mDefaultCoin.value = set
    }

    fun setInputNum(set:String){
        mInputValue.value = set
    }
}