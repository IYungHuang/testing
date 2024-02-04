package com.example.base

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log

class CustomApplication: Application() {

    companion object{
        val Tag = "Yung debug"
        val apiRetrofitServiceFly = ApiConnectManager.apiServiceFly
        val apiRetrofitServiceCurrency = ApiConnectManager.apiServiceCurrency
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
}
