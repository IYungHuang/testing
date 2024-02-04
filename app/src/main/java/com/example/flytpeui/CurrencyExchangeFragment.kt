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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.view.ContextThemeWrapper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.example.base.CustomApplication
import com.example.base.FlyTPEConstants
import com.example.flytpe.R
import com.example.flytpe.databinding.FragmentCurrencyInfoBinding
import com.example.model.FlyDetailData
import com.example.viewmoel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.view.clicks
import java.lang.Exception
import java.util.concurrent.TimeUnit


class CurrencyExchangeFragment:Fragment() {

    private var _binding: FragmentCurrencyInfoBinding? = null
    private val binding get() = _binding!!
    private var mFragmentManager: FragmentManager? = null
    private val viewModel: MainViewModel by activityViewModels()
    private var calculateBase = FlyTPEConstants.USD
    private var mMainFragment : Fragment? = null
    private var num:MutableList<String> = mutableListOf()
    private var cal:MutableList<String> = mutableListOf()
    private var fakeInPut = "0"
    private var inPut = "0"
    private var isFinAndReset = true
    private var charP :Char? = null
    private var charM :Char? = null
    private var charT :Char? = null
    private var charD :Char? = null
    private var charPoint :Char? = null
    private var targetColor = Color.BLACK

    //region 參數 - bottomSheet
    private var clBottomCalculatorBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    //endregion

    companion object {
        val newInstance by lazy { CurrencyExchangeFragment() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrencyInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFragmentManager = (requireActivity() as MainActivity).supportFragmentManager
        initView()
        initClickListener()
        initBottomSheetNavi()
        setApiDataObserver()
        setCalculatorObserver()
        setNightModeObserver()
        hideBottomNavi()
        initCheckChar()
        Handler(Looper.getMainLooper()).postDelayed({
            setDefaultCoinObserver()
            changeMoneyColor()
        },200)
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
            hideBottomNavi()
        }

    }

    private fun hideBottomNavi(){
        mMainFragment = mFragmentManager?.findFragmentByTag("mainFragment")
        (mMainFragment as MainFragment).bottomNaviSheetVisibility()
    }

    private fun initBottomSheetNavi() {
        clBottomCalculatorBehavior = BottomSheetBehavior.from(binding.includeCalculator.clBottomCalculator)
        clBottomCalculatorBehavior?.addBottomSheetCallback(bottomSheetCallback)
        clBottomCalculatorBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(this,backPressedCallback)
    }

    private val backPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            binding.imBackArrowCurrency.performClick()
        }
    }

    private fun initView(){
        binding.includeInfoViewCAD.tvMoneyType.text = FlyTPEConstants.CAD
        binding.includeInfoViewCNY.tvMoneyType.text = FlyTPEConstants.CNY
        binding.includeInfoViewEUR.tvMoneyType.text = FlyTPEConstants.EUR
        binding.includeInfoViewJPY.tvMoneyType.text = FlyTPEConstants.JPY
        binding.includeInfoViewUSD.tvMoneyType.text = FlyTPEConstants.USD
        binding.includeInfoViewAUD.tvMoneyType.text = FlyTPEConstants.AUD
    }

    private fun initCheckChar(){
        val sP = "+"
        charP = sP[0]
        val sM = "-"
        charM = sM[0]
        val sT = "*"
        charT = sT[0]
        val sD = "/"
        charD = sD[0]
        val sPoint = "."
        charPoint = sPoint[0]
    }

    private fun setApiDataObserver() {
        viewModel.rawCurrencyData.observe(viewLifecycleOwner){
            reCalculated()
        }
    }

    private fun setDefaultCoinObserver(){
        viewModel.defaultCoin.observe(viewLifecycleOwner){defaultCoin ->
            calculateBase = defaultCoin
            reCalculated()
            changeMoneyColor()
        }
    }

    private fun setCalculatorObserver(){
        viewModel.inputValue.observe(viewLifecycleOwner){
            binding.includeCalculator.edShowCalculate.text = it
        }
    }

    private fun setNightModeObserver(){
        viewModel.isNightMode.observe(viewLifecycleOwner){ isNightMode ->
            if(isNightMode){
                binding.coordContainer.setBackgroundResource(R.color.black)
                binding.imBackArrowCurrency.setImageResource(R.drawable.arrow_back_night)
                targetColor = Color.WHITE
                Color.WHITE.let {
                    binding.includeInfoViewCAD.tvMoneyExchange.setTextColor(it)
                    binding.includeInfoViewCNY.tvMoneyExchange.setTextColor(it)
                    binding.includeInfoViewEUR.tvMoneyExchange.setTextColor(it)
                    binding.includeInfoViewJPY.tvMoneyExchange.setTextColor(it)
                    binding.includeInfoViewUSD.tvMoneyExchange.setTextColor(it)
                    binding.includeInfoViewAUD.tvMoneyExchange.setTextColor(it)

                    binding.includeInfoViewCAD.tvMoneyType.setTextColor(it)
                    binding.includeInfoViewCNY.tvMoneyType.setTextColor(it)
                    binding.includeInfoViewEUR.tvMoneyType.setTextColor(it)
                    binding.includeInfoViewJPY.tvMoneyType.setTextColor(it)
                    binding.includeInfoViewUSD.tvMoneyType.setTextColor(it)
                    binding.includeInfoViewAUD.tvMoneyType.setTextColor(it)
                }
            }else{
                binding.coordContainer.setBackgroundResource(R.color.full_background)
                binding.imBackArrowCurrency.setImageResource(R.drawable.arrow_back)
                targetColor = Color.BLACK
                Color.BLACK.let {
                    binding.includeInfoViewCAD.tvMoneyExchange.setTextColor(it)
                    binding.includeInfoViewCNY.tvMoneyExchange.setTextColor(it)
                    binding.includeInfoViewEUR.tvMoneyExchange.setTextColor(it)
                    binding.includeInfoViewJPY.tvMoneyExchange.setTextColor(it)
                    binding.includeInfoViewUSD.tvMoneyExchange.setTextColor(it)
                    binding.includeInfoViewAUD.tvMoneyExchange.setTextColor(it)

                    binding.includeInfoViewCAD.tvMoneyType.setTextColor(it)
                    binding.includeInfoViewCNY.tvMoneyType.setTextColor(it)
                    binding.includeInfoViewEUR.tvMoneyType.setTextColor(it)
                    binding.includeInfoViewJPY.tvMoneyType.setTextColor(it)
                    binding.includeInfoViewUSD.tvMoneyType.setTextColor(it)
                    binding.includeInfoViewAUD.tvMoneyType.setTextColor(it)
                }
            }
            changeMoneyColor()
        }
    }

    @SuppressLint("CheckResult")
    private fun initClickListener() {
        binding.clCalculatorIcon.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            clBottomCalculatorBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.imBackArrowCurrency.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            (mMainFragment as MainFragment).showFlyInfoFragment()
            (mMainFragment as MainFragment).checkBottomTabChange(BottomTab.FlyInfo)
        }

        binding.includeInfoViewCAD.clCurrencyExInfoDetailContainer.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            calculateBase = FlyTPEConstants.CAD
            changeMoneyColor()
            if(inPut != "" && inPut.toInt() != 0){
                reCalculated(inPut.toFloat())
            }else{
                reCalculated()
            }
        }
        binding.includeInfoViewCNY.clCurrencyExInfoDetailContainer.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            calculateBase = FlyTPEConstants.CNY
            changeMoneyColor()
            if(inPut != "" && inPut.toInt() != 0){
                reCalculated(inPut.toFloat())
            }else{
                reCalculated()
            }
        }
        binding.includeInfoViewEUR.clCurrencyExInfoDetailContainer.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            calculateBase = FlyTPEConstants.EUR
            changeMoneyColor()
            if(inPut != "" && inPut.toInt() != 0){
                reCalculated(inPut.toFloat())
            }else{
                reCalculated()
            }
        }
        binding.includeInfoViewJPY.clCurrencyExInfoDetailContainer.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            calculateBase = FlyTPEConstants.JPY
            changeMoneyColor()
            if(inPut != "" && inPut.toInt() != 0){
                reCalculated(inPut.toFloat())
            }else{
                reCalculated()
            }
        }
        binding.includeInfoViewUSD.clCurrencyExInfoDetailContainer.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            calculateBase = FlyTPEConstants.USD
            changeMoneyColor()
            if(inPut != "" && inPut.toInt() != 0){
                reCalculated(inPut.toFloat())
            }else{
                reCalculated()
            }
        }
        binding.includeInfoViewAUD.clCurrencyExInfoDetailContainer.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            calculateBase = FlyTPEConstants.AUD
            changeMoneyColor()
            if(inPut != "" && inPut.toInt() != 0){
                reCalculated(inPut.toFloat())
            }else{
                reCalculated()
            }
        }

        //
        binding.includeCalculator.button01.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            if(!isFinAndReset)reSet()
            if(fakeInPut == "0")fakeInPut=""
            fakeInPut += "1"
            inPut += "1"
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.button02.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            if(!isFinAndReset)reSet()
            if(fakeInPut == "0")fakeInPut=""
            fakeInPut += "2"
            inPut += "2"
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.button03.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            if(!isFinAndReset)reSet()
            if(fakeInPut == "0")fakeInPut=""
            fakeInPut += "3"
            inPut += "3"
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.button04.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            if(!isFinAndReset)reSet()
            if(fakeInPut == "0")fakeInPut=""
            fakeInPut += "4"
            inPut += "4"
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.button05.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            if(!isFinAndReset)reSet()
            if(fakeInPut == "0")fakeInPut=""
            fakeInPut += "5"
            inPut += "5"
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.button06.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            if(!isFinAndReset)reSet()
            if(fakeInPut == "0")fakeInPut=""
            fakeInPut += "6"
            inPut += "6"
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.button07.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            if(!isFinAndReset)reSet()
            if(fakeInPut == "0")fakeInPut=""
            fakeInPut += "7"
            inPut += "7"
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.button08.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            if(!isFinAndReset)reSet()
            if(fakeInPut == "0")fakeInPut=""
            fakeInPut += "8"
            inPut += "8"
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.button09.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            if(!isFinAndReset)reSet()
            if(fakeInPut == "0")fakeInPut=""
            fakeInPut += "9"
            inPut += "9"
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.buttonZero.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            if(!isFinAndReset)reSet()
            if(fakeInPut == "0")fakeInPut=""
            fakeInPut += "0"
            inPut += "0"
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.buttonPlus.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            eachCla(Cal.P)
        }

        binding.includeCalculator.buttonMinus.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            eachCla(Cal.M)
        }

        binding.includeCalculator.buttonTimes.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            eachCla(Cal.T)
        }

        binding.includeCalculator.buttonDiv.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            eachCla(Cal.D)
        }

        binding.includeCalculator.buttonPoint.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            fakeInPut += "."
            inPut += "."
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.buttonEnter.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            doCalculate()
        }

        binding.includeCalculator.buttonCancel.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            reSet()
        }

        binding.includeCalculator.buttonBackDel.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER_CAL, TimeUnit.MILLISECONDS).subscribe{
            fakeInPut = fakeInPut.substring(0,fakeInPut.lastIndex)
            if(fakeInPut.isEmpty())fakeInPut="0"
            inPut = fakeInPut
            viewModel.setInputNum(fakeInPut)
        }

        binding.includeCalculator.imCloseSheet.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            clBottomCalculatorBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding.includeCalculator.viewCloseSheet.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            clBottomCalculatorBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun eachCla(calculation:Cal){

        if(fakeInPut.last() != charP
            && fakeInPut.last() != charM
            && fakeInPut.last() != charD
            && fakeInPut.last() != charT
            && fakeInPut.last() != charPoint
        ){
            when(calculation){
                Cal.P -> {
                    cal.add("+")
                    fakeInPut += "+"
                    inPut += "x"
                    viewModel.setInputNum(fakeInPut)
                    isFinAndReset = true
                }
                Cal.M -> {
                    cal.add("-")
                    fakeInPut += "-"
                    inPut += "x"
                    viewModel.setInputNum(fakeInPut)
                    isFinAndReset = true
                }
                Cal.T -> {
                    cal.add("*")
                    fakeInPut += "*"
                    inPut += "x"
                    viewModel.setInputNum(fakeInPut)
                    isFinAndReset = true
                }
                Cal.D -> {
                    cal.add("/")
                    fakeInPut += "/"
                    inPut += "x"
                    viewModel.setInputNum(fakeInPut)
                    isFinAndReset = true
                }
            }
        }else{
            showCalErrorSnackBar()
        }
    }

    private fun showCalErrorSnackBar() {
        val snackBar = Snackbar.make(
            requireActivity().findViewById(R.id.currencySnackBarContainer),
            getString(R.string.make_sure_input), Snackbar.LENGTH_SHORT
        )
        snackBar.setTextColor(Color.RED)

        snackBar.show()
    }

    private fun reSet() {
        fakeInPut = "0"
        inPut = ""
        viewModel.setInputNum(fakeInPut)
        num.clear()
        isFinAndReset = true
    }

    private fun doCalculate(){
       inPut.split("x").forEachIndexed { index, s ->
           if(index == 0 && s == "0"){
               num.add(s)
           }else{
               num.add(s)
           }
       }
        var ans = 0f
        if(num.size == 1){
            ans = num[0].toFloat()
        }else{
            cal.forEachIndexed { _, x ->
                when(x){
                    "+" ->{
                        ans = num[0].toFloat().plus(num[1].toFloat())
                        num.removeAt(0)
                        num.removeAt(0)
                        num.add(0,ans.toString())
                    }

                    "-" ->{
                        ans = num[0].toFloat().minus(num[1].toFloat())
                        num.removeAt(0)
                        num.removeAt(0)
                        num.add(0,ans.toString())
                    }

                    "*" ->{
                        ans = num[0].toFloat().times(num[1].toFloat())
                        num.removeAt(0)
                        num.removeAt(0)
                        num.add(0,ans.toString())
                    }

                    "/" ->{
                        try{
                            ans = num[0].toFloat().div(num[1].toFloat())
                            num.removeAt(0)
                            num.removeAt(0)
                            num.add(0,ans.toString())
                        }catch (e:Exception){
                            Log.d("calculator","e :: $e")
                        }
                    }
                }
            }
        }

        cal.clear()

        val checkTarget = ans.toString().split(".")[1]
        val finalAns = if(checkTarget.isNotEmpty() && checkTarget.toFloat() > 0){
            ans.toString()
        } else{
            ans.toInt().toString()
        }

        num.clear()
        fakeInPut = finalAns
        inPut = finalAns
        viewModel.setInputNum(fakeInPut)
        reCalculated(ans)
        isFinAndReset = false
    }

    private fun reCalculated(timesCalculateBase:Float = 1f){
        when(calculateBase){
            FlyTPEConstants.CAD ->{
                binding.includeInfoViewCAD.tvMoneyExchange.text = 1.times(timesCalculateBase).toString()
                binding.includeInfoViewCNY.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.cny?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.cad!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewEUR.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.eur?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.cad!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewJPY.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.jpy?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.cad!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewUSD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.usd?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.cad!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewAUD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.aud?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.cad!!.toFloat())?.times(timesCalculateBase).toString()
            }
            FlyTPEConstants.CNY ->{
                binding.includeInfoViewCAD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.cad?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.cny!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewCNY.tvMoneyExchange.text = 1.times(timesCalculateBase).toString()
                binding.includeInfoViewEUR.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.eur?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.cny!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewJPY.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.jpy?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.cny!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewUSD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.usd?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.cny!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewAUD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.aud?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.cny!!.toFloat())?.times(timesCalculateBase).toString()
            }
            FlyTPEConstants.EUR ->{
                binding.includeInfoViewCAD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.cad?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.eur!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewCNY.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.cny?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.eur!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewEUR.tvMoneyExchange.text = 1.times(timesCalculateBase).toString()
                binding.includeInfoViewJPY.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.jpy?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.eur!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewUSD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.usd?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.eur!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewAUD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.aud?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.eur!!.toFloat())?.times(timesCalculateBase).toString()
            }
            FlyTPEConstants.JPY ->{
                binding.includeInfoViewCAD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.cad?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.jpy!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewCNY.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.cny?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.jpy!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewEUR.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.eur?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.jpy!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewJPY.tvMoneyExchange.text = 1.times(timesCalculateBase).toString()
                binding.includeInfoViewUSD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.usd?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.jpy!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewAUD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.aud?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.jpy!!.toFloat())?.times(timesCalculateBase).toString()
            }
            FlyTPEConstants.USD  ->{
                binding.includeInfoViewCAD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.cad?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.usd!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewCNY.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.cny?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.usd!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewEUR.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.eur?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.usd!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewJPY.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.jpy?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.usd!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewUSD.tvMoneyExchange.text = 1.times(timesCalculateBase).toString()
                binding.includeInfoViewAUD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.aud?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.usd!!.toFloat())?.times(timesCalculateBase).toString()
            }
            FlyTPEConstants.AUD->{
                binding.includeInfoViewCAD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.cad?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.aud!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewCNY.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.cny?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.aud!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewEUR.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.eur?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.aud!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewJPY.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.jpy?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.aud!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewUSD.tvMoneyExchange.text = viewModel.rawCurrencyData.value?.data?.usd?.toFloat()?.div(viewModel.rawCurrencyData.value?.data?.aud!!.toFloat())?.times(timesCalculateBase).toString()
                binding.includeInfoViewAUD.tvMoneyExchange.text = 1.times(timesCalculateBase).toString()
            }
        }
    }

    private fun changeMoneyColor(){
        clBottomCalculatorBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        when(calculateBase){
            FlyTPEConstants.CAD ->{
                binding.includeInfoViewCAD.tvMoneyType.setTextColor(Color.RED)
                binding.includeInfoViewCNY.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewEUR.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewJPY.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewUSD.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewAUD.tvMoneyType.setTextColor(targetColor)
            }
            FlyTPEConstants.CNY ->{
                binding.includeInfoViewCAD.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewCNY.tvMoneyType.setTextColor(Color.RED)
                binding.includeInfoViewEUR.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewJPY.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewUSD.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewAUD.tvMoneyType.setTextColor(targetColor)
            }
            FlyTPEConstants.EUR ->{
                binding.includeInfoViewCAD.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewCNY.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewEUR.tvMoneyType.setTextColor(Color.RED)
                binding.includeInfoViewJPY.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewUSD.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewAUD.tvMoneyType.setTextColor(targetColor)
            }
            FlyTPEConstants.JPY ->{
                binding.includeInfoViewCAD.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewCNY.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewEUR.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewJPY.tvMoneyType.setTextColor(Color.RED)
                binding.includeInfoViewUSD.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewAUD.tvMoneyType.setTextColor(targetColor)
            }
            FlyTPEConstants.USD ->{
                binding.includeInfoViewCAD.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewCNY.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewEUR.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewJPY.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewUSD.tvMoneyType.setTextColor(Color.RED)
                binding.includeInfoViewAUD.tvMoneyType.setTextColor(targetColor)
            }
            FlyTPEConstants.AUD ->{
                binding.includeInfoViewCAD.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewCNY.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewEUR.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewJPY.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewUSD.tvMoneyType.setTextColor(targetColor)
                binding.includeInfoViewAUD.tvMoneyType.setTextColor(Color.RED)
            }
        }
    }

    private var bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            requireActivity().runOnUiThread {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {

                    }

                    else -> {

                    }
                }
            }

        }
    }
}

enum class Cal{
    P,M,T,D
}