package com.example.flytpeui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.example.base.FlyTPEConstants
import com.example.flytpe.R
import com.example.flytpe.databinding.FragmentSettingBinding
import com.example.viewmoel.MainViewModel
import com.jakewharton.rxbinding4.view.clicks
import java.util.concurrent.TimeUnit

class SettingFragment: Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private var mFragmentManager: FragmentManager? = null
    private val viewModel: MainViewModel by activityViewModels()
    private val spinnerList = listOf("AUD","CAD","CNY","EUR","JPY","USD")

    companion object {
        val newInstance by lazy { SettingFragment()}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFragmentManager = (requireActivity() as MainActivity).supportFragmentManager
        initClickListener()
        initView()
        setSpinnerDefaultValueObserver()
        setNightModeObserver()
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
            binding.imBackArrow.performClick()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initView() {
        binding.switchBtn.isChecked = (requireActivity() as MainActivity).getNightMode()
        //
        binding.spinnerCoin.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
               val selectedT = parent.getChildAt(0) as TextView
                selectedT.setTextColor(Color.WHITE)
               val sharedPreference = (requireActivity() as MainActivity).sharedPreferences
                sharedPreference.edit().putString(FlyTPEConstants.SET_DEFAULT_COIN, parent.selectedItem.toString()).apply()
                viewModel.setDefaultCoin(parent.selectedItem.toString())
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    private fun setNightModeObserver(){
        viewModel.isNightMode.observe(viewLifecycleOwner){
            if(it){
                binding.tvNightNode.setTextColor(Color.WHITE)
                binding.tvDefaultCoin.setTextColor(Color.WHITE)
                binding.spinnerCoin.setPopupBackgroundResource(R.color.full_background)
            }else {
                binding.tvNightNode.setTextColor(Color.BLACK)
                binding.tvDefaultCoin.setTextColor(Color.BLACK)
                binding.spinnerCoin.setPopupBackgroundResource(R.color.full_background)
            }
        }
    }

    private fun setSpinnerDefaultValueObserver(){
        //
        viewModel.defaultCoin.observe(viewLifecycleOwner){defaultCoin ->
            spinnerList.filterIndexed { index, s ->
                if(s == defaultCoin){
                    binding.spinnerCoin.setSelection(index)
                }
                false
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun initClickListener() {
        binding.imBackArrow.clicks().throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS).subscribe{
            (requireActivity() as MainActivity).showMainFragment()
        }

        binding.switchBtn.setOnCheckedChangeListener { _, checked ->
            if(checked){
                (requireActivity() as MainActivity).setNightMode(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.clSettingContainer.setBackgroundResource(R.color.black)
                binding.imBackArrow.setImageResource(R.drawable.arrow_back_night)
            }else{
                (requireActivity() as MainActivity).setNightMode(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.clSettingContainer.setBackgroundResource(R.color.full_background)
                binding.imBackArrow.setImageResource(R.drawable.arrow_back)
            }

        }
    }

}
