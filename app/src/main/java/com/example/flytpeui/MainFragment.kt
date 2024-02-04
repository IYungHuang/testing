package com.example.flytpeui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.example.base.CustomApplication
import com.example.base.FlyTPEConstants
import com.example.flytpe.R
import com.example.flytpe.databinding.FragmentMainBinding
import com.example.flytpeui.uiinterface.OnDayNightStateChanged
import com.example.viewmoel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding4.view.clicks
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.pow

class MainFragment: Fragment(), OnDayNightStateChanged {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    var mFragmentManager: FragmentManager? = null
    private var currentFragment: Fragment? = null
    private var mAirInfoFragment: AirInfoFragment? = null
    private var mCurrencyExchangeFragment: CurrencyExchangeFragment? = null
    private val viewModel: MainViewModel by activityViewModels()

    //region 參數 - bottomSheet
    private var clBottomNaviBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    //endregion

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFragmentManager = (requireActivity() as MainActivity).supportFragmentManager
        initBottomSheetNavi()
        initClickListener()
        initView()
    }

    private fun initBottomSheetNavi() {
        clBottomNaviBehavior = BottomSheetBehavior.from(binding.includeBottom.clBottomNavi)
        clBottomNaviBehavior?.peekHeight = 70
        clBottomNaviBehavior?.addBottomSheetCallback(bottomSheetCallback)
        clBottomNaviBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initView() {
        binding.includeBottom.imArrowHide.animate().rotation(-360F)
        binding.includeBottom.clFlyInfo.performClick()
    }

    override fun onResume() {
        super.onResume()
        handleBackPress()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        backPressedCallback.isEnabled = false
        if(!hidden){
            backPressedCallback.isEnabled = true
            handleBackPress()

        }

    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this,backPressedCallback)
    }

    private val backPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            requireActivity().moveTaskToBack(true)
        }
    }

    @SuppressLint("CheckResult")
    private fun initClickListener() {
        binding.includeBottom.imSettingTab.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS)
            .subscribe{
                (requireActivity() as MainActivity).setCircleProgressIndicatorInVisible()
                (requireActivity() as MainActivity).showSettingFragment()
            }

        binding.includeBottom.imBottomSheetControllerTab.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS)
            .subscribe{
                bottomNaviSheetVisibility()
            }

        binding.includeBottom.clFlyInfo.clicks()
            .throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS)
            .subscribe {
                checkBottomTabChange(BottomTab.FlyInfo)
                showFlyInfoFragment()
            }

        binding.includeBottom.clExRate.clicks()
            .throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS)
            .subscribe {
                checkBottomTabChange(BottomTab.CurrencyEx)
                Handler(Looper.getMainLooper()).postDelayed({
                    showCurrencyFragment()
                },200)
            }
    }

    fun bottomNaviSheetVisibility(){
        if(clBottomNaviBehavior?.state == BottomSheetBehavior.STATE_EXPANDED){
            clBottomNaviBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }else{
            clBottomNaviBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun checkBottomTabChange(tab: BottomTab) {
        when (tab) {
            BottomTab.FlyInfo -> {
                if((requireActivity() as MainActivity).getNightMode()){
                    binding.includeBottom.imPageFlyInfo.setImageResource(R.drawable.airplane_white)
                    binding.includeBottom.tvFlyOrder.setTextColor(Color.WHITE)
                }else{
                    binding.includeBottom.imPageFlyInfo.setImageResource(R.drawable.airplane_black)
                    binding.includeBottom.tvFlyOrder.setTextColor(Color.BLACK)
                }
                binding.includeBottom.imPageCurrencyInfo.setImageResource(R.drawable.money_grey)
                binding.includeBottom.tvExRate.setTextColor(Color.GRAY)
                binding.includeBottom.imPageFlyInfo.startAnimation((requireActivity() as MainActivity).animationBubble)

            }

            BottomTab.CurrencyEx -> {
                if((requireActivity() as MainActivity).getNightMode()){
                    binding.includeBottom.imPageCurrencyInfo.setImageResource(R.drawable.money_white)
                    binding.includeBottom.tvExRate.setTextColor(Color.WHITE)
                }else{
                    binding.includeBottom.imPageCurrencyInfo.setImageResource(R.drawable.money_black)
                    binding.includeBottom.tvExRate.setTextColor(Color.BLACK)
                }
                binding.includeBottom.imPageFlyInfo.setImageResource(R.drawable.airplane_grey)
                binding.includeBottom.tvFlyOrder.setTextColor(Color.GRAY)
                binding.includeBottom.imPageCurrencyInfo.startAnimation((requireActivity() as MainActivity).animationBubble)
            }
        }
    }

    //
    fun showFlyInfoFragment() {
        if(currentFragment !is AirInfoFragment){
            currentFragment?.let {
                mFragmentManager?.inTransactionNoAnimationWithSlide {
                    hide(it)
                }
            }
            if (mAirInfoFragment == null) {
                mAirInfoFragment = AirInfoFragment.newInstance
                mFragmentManager?.inTransactionNoAnimation {
                    add(R.id.frMainFragContainer, mAirInfoFragment!!, "AirInfoFragment")
                    show(mAirInfoFragment!!)
                }
            } else {
                mFragmentManager?.inTransactionNoAnimation {
                    show(mAirInfoFragment!!)
                }
            }
            currentFragment = mAirInfoFragment
        }
    }

    private fun showCurrencyFragment() {
        if(currentFragment !is CurrencyExchangeFragment){
            currentFragment?.let {
                mFragmentManager?.inTransactionNoAnimation {
                    hide(it)
                }
            }
            if (mCurrencyExchangeFragment == null) {
                mCurrencyExchangeFragment = CurrencyExchangeFragment.newInstance
                mFragmentManager?.inTransactionStillSlide {
                    add(R.id.frMainFragContainer, mCurrencyExchangeFragment!!, "AirInfoFragment")
                    show(mCurrencyExchangeFragment!!)
                }
            } else {
                mFragmentManager?.inTransactionStillSlide {
                    show(mCurrencyExchangeFragment!!)
                }
            }
            currentFragment = mCurrencyExchangeFragment
        }
    }

    private var bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            requireActivity().runOnUiThread {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.includeBottom.imArrowHide.animate().rotation(-180F)
                        binding.includeBottom.tvBottomSheetExpectState.text = binding.includeBottom.tvBottomSheetExpectState.context.getString(R.string.expandBottomSheet)
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.includeBottom.imArrowHide.animate().rotation(-360F)
                        binding.includeBottom.tvBottomSheetExpectState.text = binding.includeBottom.tvBottomSheetExpectState.context.getString(R.string.hiddenBottomSheet)
                    }

                    else -> {

                    }
                }
            }

        }
    }

    override fun onDayNightApplied(state: Int) {
        if (state == OnDayNightStateChanged.DAY){
            binding.includeBottom.clbottomTab.setBackgroundResource(R.color.blue_gray)
            binding.includeBottom.imSettingTab.setImageResource(R.drawable.bottom_sheet_tab_setting)
            binding.includeBottom.imBottomSheetControllerTab.setImageResource(R.drawable.bottom_sheet_tab)
            binding.includeBottom.imArrowHide.setImageResource(R.drawable.arrow_down)
            binding.includeBottom.imSetting.setImageResource(R.drawable.settings)
            binding.includeBottom.imPageFlyInfo
            binding.includeBottom.tvBottomSheetExpectState.setTextColor(Color.BLACK)
            binding.includeBottom.tvSetting.setTextColor(Color.BLACK)
            binding.includeBottom.imPageCurrencyInfo
            viewModel.setNightMode(false)

            if(currentFragment is AirInfoFragment){
                binding.includeBottom.imPageFlyInfo.setImageResource(R.drawable.airplane_black)
                binding.includeBottom.tvFlyOrder.setTextColor(Color.BLACK)
            }else{
                binding.includeBottom.imPageCurrencyInfo.setImageResource(R.drawable.money_black)
                binding.includeBottom.tvExRate.setTextColor(Color.BLACK)
            }
        }else{
            binding.includeBottom.clbottomTab.setBackgroundResource(R.color.blue_gray_night)
            binding.includeBottom.imSettingTab.setImageResource(R.drawable.bottom_sheet_tab_setting_night)
            binding.includeBottom.imBottomSheetControllerTab.setImageResource(R.drawable.bottom_sheet_tab_night)
            binding.includeBottom.imArrowHide.setImageResource(R.drawable.arrow_down_night)
            binding.includeBottom.imSetting.setImageResource(R.drawable.settings_night)
            binding.includeBottom.tvBottomSheetExpectState.setTextColor(Color.WHITE)
            binding.includeBottom.tvSetting.setTextColor(Color.WHITE)
            binding.includeBottom.imPageFlyInfo
            binding.includeBottom.imPageCurrencyInfo
            viewModel.setNightMode(true)

            if(currentFragment is AirInfoFragment){
                binding.includeBottom.imPageFlyInfo.setImageResource(R.drawable.airplane_white)
                binding.includeBottom.tvFlyOrder.setTextColor(Color.WHITE)
            }else{
                binding.includeBottom.imPageCurrencyInfo.setImageResource(R.drawable.money_white)
                binding.includeBottom.tvExRate.setTextColor(Color.WHITE)
            }
        }
    }
}

enum class BottomTab {
    FlyInfo,CurrencyEx
}