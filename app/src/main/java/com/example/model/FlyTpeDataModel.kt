package com.example.model

import com.example.base.FlyTPEConstants
import com.example.flytpe.R
import com.google.gson.annotations.SerializedName

data class CurrencyData(
    @SerializedName(FlyTPEConstants.DATA)
    var data:GetCurrencyDetailData? = null
)

data class FlyDetailData(
    @SerializedName(FlyTPEConstants.FLY_TYPE)
    var flyType: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.AIR_LINE_ID)
    var airLineId: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.AIR_LINE)
    var airline: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.FLIGHT_NUMBER)
    var flightNumber: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.DEPARTURE_AIRPORT_ID)
    var departureAirportID: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.DEPARTURE_AIRPORT)
    var departureAirport: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.ARRIVAL_AIRPORT_ID)
    var arrivalAirportID: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.ARRIVAL_AIRPORT)
    var arrivalAirport: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.SCHEDULE_TIME)
    var scheduleTime: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.ACTUAL_TIME)
    var actualTime: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.ESTIMATED_TIME)
    var estimatedTime: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.REMARK)
    var remark: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.TERMINAL)
    var terminal: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.GATE)
    var gate: String? = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.UPDATE_TIME)
    var updateTime: String = FlyTPEConstants.EMPTY_STRING,
    var isNightMode: Boolean = false,
    var isFake:Boolean = false
):IItemLayoutRes{
    override var mtype: Int
        get() = R.layout.item_fly_info
        set(value) {}
}

data class GetCurrencyDetailData(
    @SerializedName(FlyTPEConstants.AUD)
    var aud: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.CAD)
    var cad: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.CNY)
    var cny: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.EUR)
    var eur: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.JPY)
    var jpy: String = FlyTPEConstants.EMPTY_STRING,
    @SerializedName(FlyTPEConstants.USD)
    var usd: String = FlyTPEConstants.EMPTY_STRING
)

