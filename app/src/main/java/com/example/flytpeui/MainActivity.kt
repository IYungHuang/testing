package com.example.flytpeui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.base.CustomApplication
import com.example.base.FlyTPEConstants
import com.example.flytpe.R
import com.example.flytpe.databinding.ActivityMainBinding
import com.example.flytpeui.uiinterface.OnDayNightStateChanged
import com.example.viewmoel.AnyViewModelFactory
import com.example.viewmoel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.pow


class MainActivity : AppCompatActivity() {
    //region 參數
    private lateinit var binding: ActivityMainBinding

    //viewModel 工廠 依賴注入建立viewModel
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this,
            AnyViewModelFactory(
                CustomApplication.apiRetrofitServiceFly,
                CustomApplication.apiRetrofitServiceCurrency
            )
        )[MainViewModel::class.java]
    }

    private var heartBeatCheckFlyInfoDisposable: Disposable? = null
    private val heartBeatDelay = 10L
    private val heartBeatPeriod = 10L

    lateinit var sharedPreferences: SharedPreferences

    //UI相關
    private lateinit var mFragmentManager: FragmentManager
    var currentFragment: Fragment? = null
    private var mainFragment: MainFragment? = null
    private var settingFragment: SettingFragment? = null
    private var isNightMode = false

    //
    lateinit var animationBubble: Animation
    private lateinit var newInterpolator: BubbleInterpolator
    private val resultLauncherSendMailActivity: ActivityResultLauncher<Intent> =
        registerForActivityResult (ActivityResultContracts.StartActivityForResult()) { _ ->
            showRetryGetApiDataSnackBar()
        }
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setSharedPreference()
        checkSharedPreferenceData()
        if (getNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        changeSystemStatusBarColor()
        viewModel.setNightMode(getNightMode())
        setContentView(binding.root)
        checkInternetAndGetData()
        initAnimationObject()
        initView()
        setApiDataObserver()
        setRetrofitErrorFly()
        setRetrofitErrorCurrency()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags != Configuration.UI_MODE_NIGHT_NO) {
            applyDayNight(OnDayNightStateChanged.NIGHT)
        } else {
            applyDayNight(OnDayNightStateChanged.DAY)
        }
    }

    private fun applyDayNight(state: Int) {

        supportFragmentManager.fragments.forEach {
            if (it is OnDayNightStateChanged) {
                it.onDayNightApplied(state)
            }
        }
    }

    private fun setSharedPreference() {
        sharedPreferences =
            this.getSharedPreferences(FlyTPEConstants.USER_INFO, Context.MODE_PRIVATE)
    }

    private fun setApiDataObserver() {
        viewModel.rawFlyDataArrival.observe(this) { listFlyInfoDataArrival ->
            if (listFlyInfoDataArrival.isNotEmpty()) {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.circleIndicator.visibility = View.INVISIBLE
                }, 500L)
            }
        }
    }

    private fun setRetrofitErrorFly() {
        viewModel.retrofitErrorFly.observe(this) {
            showCalErrorSnackBar(getString(R.string.flyInfoRetrofitError) + it)
            closeHeartBeat()
        }
    }

    private fun setRetrofitErrorCurrency() {
        viewModel.retrofitErrorCurrency.observe(this) {
            showCalErrorSnackBar(getString(R.string.currencyInfoRetrofitError) + it)
            closeHeartBeat()
        }
    }

    private fun showCalErrorSnackBar(error: String) {

        val snackBar = Snackbar.make(
            findViewById(R.id.mainActivitySnackBarContainer),
            error, Snackbar.LENGTH_INDEFINITE
        ).setAction("傳送錯誤") {
            sendError(error)
        }
        snackBar.setTextColor(Color.RED)
        snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.blue_gray))

        snackBar.show()
    }

    private fun showRetryGetApiDataSnackBar() {

        val snackBar = Snackbar.make(
            findViewById(R.id.mainActivitySnackBarContainer),
            "重新取得資料", Snackbar.LENGTH_INDEFINITE
        ).setAction("GO!") {
            checkInternetAndGetData()
        }
        snackBar.setTextColor(Color.RED)
        snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.full_background))

        snackBar.show()
    }

    private fun sendError(error: String){
        val shareText: String = error
        val selectorIntent = Intent(Intent.ACTION_SENDTO)
        selectorIntent.data = Uri.parse("mailto:")

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("bioideastw@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "注意!!$shareText")
        emailIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        emailIntent.selector = selectorIntent
        resultLauncherSendMailActivity.launch(emailIntent)
    }

    private fun checkSharedPreferenceData(): Boolean {
        isNightMode = sharedPreferences.getBoolean(FlyTPEConstants.SET_NIGHT_MODE, false)
        val defaultCoin = sharedPreferences.getString(FlyTPEConstants.SET_DEFAULT_COIN, "USD")
        viewModel.setNightMode(isNightMode)
        viewModel.setDefaultCoin(defaultCoin ?: "USD")
        return isNightMode
    }

    fun getNightMode(): Boolean {
        return isNightMode
    }

    fun setNightMode(setNightMode: Boolean) {
        isNightMode = setNightMode
        sharedPreferences.edit().putBoolean(FlyTPEConstants.SET_NIGHT_MODE, setNightMode).apply()
        changeSystemStatusBarColor()
    }

    private fun changeSystemStatusBarColor() {
        val window = window
        val decorView = window.decorView
        val wic = WindowInsetsControllerCompat(window, decorView)
        wic.isAppearanceLightStatusBars = false
        window.statusBarColor = ContextCompat.getColor(
            this,
            getStatusColor()
        )
        setStatusBarLightTextNewApi(window)
        return
    }

    private fun getStatusColor(): Int {
        return if (!getNightMode()) return R.color.full_background else R.color.black
    }

    private fun setStatusBarLightTextNewApi(window: Window) {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeHeartBeat()
    }

    //region 確認網路狀況
    private fun checkInternetAndGetData() {
        if (isInternetAvailable()) {
            getApiData()
            if (heartBeatCheckFlyInfoDisposable == null) {
                startHeartBeatRefreshData()
            }
        } else {
            val ctw = ContextThemeWrapper(this, R.style.CustomSnackbarTheme)
            val snackBar = Snackbar.make(
                ctw,
                findViewById(R.id.clMaincontainer),
                getString(R.string.make_sure_internet), Snackbar.LENGTH_INDEFINITE
            )
            snackBar.setTextColor(Color.BLACK)
            snackBar.setActionTextColor(ContextCompat.getColor(this, R.color.full_background))

            snackBar.setAction("確認已連線") {
                checkInternetAndGetData()
                binding.circleIndicator.visibility = View.VISIBLE
            }.show()
        }
    }
    //endregion

    private fun initAnimationObject() {
        animationBubble = AnimationUtils.loadAnimation(this, R.anim.scale_bubble)
        newInterpolator = BubbleInterpolator(0.2, 20.0)
        animationBubble.interpolator = newInterpolator
    }

    private fun initView() {
        mFragmentManager = supportFragmentManager

        mainFragment = MainFragment.newInstance()
        mFragmentManager.inTransactionFadeShow {
            add(R.id.frContainer, mainFragment!!, "mainFragment")
            show(mainFragment!!)
        }
        currentFragment = mainFragment
    }

    fun showSettingFragment() {
        currentFragment?.let {
            mFragmentManager.inTransactionNoAnimation {
                hide(it)
            }
        }
        if (settingFragment == null) {
            settingFragment = SettingFragment.newInstance
            mFragmentManager.inTransactionStillSlide {
                add(R.id.frContainer, settingFragment!!, "settingFragment")
                show(settingFragment!!)
            }
        } else {
            mFragmentManager.inTransactionStillSlide {
                show(settingFragment!!)
            }
        }
        currentFragment = settingFragment

    }

    fun setCircleProgressIndicatorInVisible() {
        binding.circleIndicator.visibility = View.INVISIBLE
    }

    fun showMainFragment() {
        currentFragment?.let {
            mFragmentManager.inTransactionNoAnimationWithSlide {
                hide(it)
            }
        }
        mFragmentManager.inTransactionNoAnimation {
            show(mainFragment!!)
        }
        currentFragment = mainFragment
        changeSystemStatusBarColor()
    }

    //region 連結api取得資料
    private fun getApiData() {
        viewModel.getFlyData(FlyTPEConstants.TAKE_OFF_FLY, FlyTPEConstants.AIRPORT)
        viewModel.getFlyData(FlyTPEConstants.ARRIVAL_FLY, FlyTPEConstants.AIRPORT)
        viewModel.getCurrencyData()
    }
    //endregion

    private fun startHeartBeatRefreshData() {
        heartBeatCheckFlyInfoDisposable =
            Observable.interval(heartBeatDelay, heartBeatPeriod, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    checkInternetAndGetData()
                    if (currentFragment !is SettingFragment) {
                        binding.circleIndicator.visibility = View.VISIBLE
                    }
                }
    }

    //關閉心跳
    private fun closeHeartBeat() {
        heartBeatCheckFlyInfoDisposable?.dispose()
        setCircleProgressIndicatorInVisible()
    }

    //region 回傳網路狀況
    private fun isInternetAvailable(): Boolean {
        val result: Boolean
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actionNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities)
            ?: return false
        result = when {
            actionNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                true
            }

            actionNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                true
            }

            actionNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                true
            }

            actionNetwork.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                true
            }

            else -> false
        }

        return result
    }
    //endregion

}

inline fun FragmentManager.inTransactionNoAnimation(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().setCustomAnimations(R.anim.no_animation, R.anim.no_animation).setTransition(
        FragmentTransaction.TRANSIT_NONE
    ).func()
        .commitAllowingStateLoss()
}

inline fun FragmentManager.inTransactionNoAnimationWithSlide(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().setCustomAnimations(R.anim.no_animation, R.anim.left_to_right_exit)
        .setTransition(
            FragmentTransaction.TRANSIT_NONE
        ).func()
        .commitAllowingStateLoss()
}

inline fun FragmentManager.inTransactionStillSlide(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().setCustomAnimations(R.anim.right_to_left_enter, R.anim.no_animation)
        .setTransition(
            FragmentTransaction.TRANSIT_NONE
        ).func()
        .commitAllowingStateLoss()
}

inline fun FragmentManager.inTransactionFadeShow(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().setTransition(FragmentTransaction.TRANSIT_NONE).func()
        .commitAllowingStateLoss()
}

class BubbleInterpolator(var a: Double = 1.0, var f: Double) : Interpolator {

    override fun getInterpolation(input: Float): Float {
        val params = -1 * Math.E.pow(-input / a) * cos(f * input) + 1
        return params.toFloat()
    }
}
