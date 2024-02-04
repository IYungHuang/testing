package com.example.flytpeui

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.base.CustomApplication
import com.example.flytpe.R
import com.example.flytpe.databinding.FragmentFlyInfoBinding
import com.example.flytpeui.uiinterface.OnDayNightStateChanged
import com.example.flytpeui.uiinterface.ScrollDirectCallback
import com.example.flytpeui.vpadapter.AirInfoViewPagerFragmentAdapter
import com.example.model.FlyDetailData
import com.example.viewmoel.MainViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AirInfoFragment:Fragment(),ScrollDirectCallback, OnDayNightStateChanged {

    private lateinit var _binding : FragmentFlyInfoBinding
    private val binding get() = _binding
    private lateinit var airInfoFragmentVpAdapter: AirInfoViewPagerFragmentAdapter
    private lateinit var pageViewList: MutableList<Fragment>
    private lateinit var takeOffFlyFragment: TakeOffFlyFragment
    private lateinit var arrivalTakeOffFlyFragment: ArrivalFlyFragment
    private val viewModel: MainViewModel by activityViewModels()

    companion object {
        val newInstance by lazy { AirInfoFragment() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFlyInfoBinding.inflate(inflater,container,false)
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setNightModeObserver()
    }

    private fun initView() {
        //tablayout and viewpager2
        binding.flyTabLayout.tabRippleColor = null
        binding.flyTabLayout.addOnTabSelectedListener(tabListener)
        takeOffFlyFragment = TakeOffFlyFragment.newInstance(this)
        arrivalTakeOffFlyFragment = ArrivalFlyFragment.newInstance(this)
        pageViewList = mutableListOf(takeOffFlyFragment,arrivalTakeOffFlyFragment)
        airInfoFragmentVpAdapter = AirInfoViewPagerFragmentAdapter(pageViewList,this)
        binding.vpFly.adapter = airInfoFragmentVpAdapter
        binding.vpFly.offscreenPageLimit = 2
        TabLayoutMediator(binding.flyTabLayout,binding.vpFly){ tab, position ->
            val mLayoutInflater = this.layoutInflater
            val tabview = mLayoutInflater.inflate(R.layout.blank_tab_item,null)
            if(position == 1){
                val tvtext = tabview.findViewById<TextView>(R.id.tabNormal)
                val tvtextbold = tabview.findViewById<TextView>(R.id.tabBold)
                val imAirPlane = tabview.findViewById<ImageView>(R.id.imPlane)
                tvtext.textSize = 18f
                tvtextbold.textSize = 18f
                tvtext.text = tvtext.context.getString(R.string.arrivalTag)
                tvtextbold.text = tvtextbold.context.getString(R.string.arrivalTag)
                imAirPlane.setImageResource(R.drawable.land_grey)
                tab.customView = tabview
            }else{
                val tvtext = tabview.findViewById<TextView>(R.id.tabNormal)
                val tvtextbold = tabview.findViewById<TextView>(R.id.tabBold)
                val imAirPlane = tabview.findViewById<ImageView>(R.id.imPlane)
                tvtext.textSize = 18f
                tvtextbold.textSize = 18f
                tvtext.text = tvtext.context.getString(R.string.takeOffTag)
                tvtextbold.text = tvtextbold.context.getString(R.string.takeOffTag)
                imAirPlane.setImageResource(R.drawable.takeoff_black)
                tab.customView = tabview
            }
        }.attach()
        binding.vpFly.isSaveEnabled = false
        binding.vpFly.isUserInputEnabled = true
    }

    private fun setNightModeObserver(){
        viewModel.isNightMode.observe(viewLifecycleOwner){ isNightMode ->
            if (!isNightMode){
                binding.clFlyFragContainer.setBackgroundResource(R.color.full_background)
                binding.flyTabLayout.setBackgroundResource(R.color.full_background)
            }else{
                binding.clFlyFragContainer.setBackgroundResource(R.color.colorPrimaryNight)
                binding.flyTabLayout.setBackgroundResource(R.color.colorPrimaryNight)
            }
        }
    }

    private val tabListener = object : TabLayout.OnTabSelectedListener{
        override fun onTabSelected(tab: TabLayout.Tab) {

            val view = tab.customView as ViewGroup
            val im1 = view.getChildAt(0) as ImageView
            val tv1 = view.getChildAt(1) as TextView
            val tv2 = view.getChildAt(2) as TextView

            tv1.visibility = View.INVISIBLE
            tv2.visibility = View.VISIBLE
            if(tab.position == 0){
                im1.setImageResource(R.drawable.takeoff_black)
            }else{
                im1.setImageResource(R.drawable.land_black)
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            val view = tab.customView as ViewGroup
            val im1 = view.getChildAt(0) as ImageView
            val tv1 = view.getChildAt(1) as TextView
            val tv2 = view.getChildAt(2) as TextView

            tv1.visibility = View.VISIBLE
            tv2.visibility = View.INVISIBLE
            if(tab.position == 0){
                im1.setImageResource(R.drawable.takeoff_grey)
            }else{
                im1.setImageResource(R.drawable.land_grey)
            }

        }

        override fun onTabReselected(tab: TabLayout.Tab?) {

        }
    }

    override fun getScrollDirection(direct: Int) {
        super.getScrollDirection(direct)
        animationForTabLayout(direct)
    }

    private fun animationForTabLayout(direct: Int) {
        val valueAnimatorVerticalBias: ValueAnimator
        if(direct > 0){
            valueAnimatorVerticalBias = ValueAnimator.ofInt (55, 1)
                .setDuration(300)
        }else{
            valueAnimatorVerticalBias = ValueAnimator.ofInt (1, 55)
                .setDuration(300)
        }
        valueAnimatorVerticalBias.addUpdateListener { animation ->
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.clFlyFragContainer)
            constraintSet.setDimensionRatio(R.id.clTabContainer,"315:${animation.animatedValue}")
            constraintSet.applyTo(binding.clFlyFragContainer)
        }
        valueAnimatorVerticalBias.start()
    }

    override fun onDayNightApplied(state: Int) {
        if (state == OnDayNightStateChanged.DAY){
            binding.clFlyFragContainer.setBackgroundResource(R.color.full_background)
            binding.flyTabLayout.setBackgroundResource(R.color.full_background)
        }else{
            binding.clFlyFragContainer.setBackgroundResource(R.color.colorPrimaryNight)
            binding.flyTabLayout.setBackgroundResource(R.color.colorPrimaryNight)
        }
    }
}

