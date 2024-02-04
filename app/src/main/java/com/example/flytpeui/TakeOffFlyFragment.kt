package com.example.flytpeui

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.base.CustomApplication
import com.example.base.FlyTPEConstants
import com.example.flytpe.R
import com.example.flytpe.databinding.FragmentInfoDetailBinding
import com.example.flytpeui.rcadapter.RcAdapter
import com.example.flytpeui.uiinterface.ScrollDirectCallback
import com.example.model.FlyDetailData
import com.example.viewmoel.MainViewModel
import com.jakewharton.rxbinding4.view.clicks
import java.util.concurrent.TimeUnit

class TakeOffFlyFragment(private val scrollDirectionCallBack: ScrollDirectCallback) : Fragment(){

    private lateinit var _binding: FragmentInfoDetailBinding
    private val binding get() = _binding
    private var mAdapter: RcAdapter? = null
    private var isRunningAnimation = false
    private val viewModel: MainViewModel by activityViewModels()

    companion object {
        fun newInstance(scrollDirectionCallBack: ScrollDirectCallback): TakeOffFlyFragment {
            return TakeOffFlyFragment(scrollDirectionCallBack,)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initClickListener()
        setNightModeObserver()
        setApiDataObserver()
    }

    private fun initView() {
        mAdapter = RcAdapter(
            mutableListOf(
                FlyDetailData(isFake = true),
                FlyDetailData(isFake = true),
                FlyDetailData(isFake = true),
                FlyDetailData(isFake = true)
            )
        )
        binding.rcFlyInfo.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rcFlyInfo.adapter = mAdapter
        binding.rcFlyInfo.setHasFixedSize(true)
        val layoutAnimation =
            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.rclaoutanimation)
        binding.rcFlyInfo.layoutAnimation = layoutAnimation
        binding.rcFlyInfo.addOnScrollListener(rcOnScrollListener)
    }

    private fun setApiDataObserver() {
        viewModel.rawFlyDataTakeOff.observe(viewLifecycleOwner){ listFlyInfoDataTakeoff->
            listFlyInfoDataTakeoff.forEach {
                it.isNightMode = viewModel.isNightMode.value!!
            }
            Handler(Looper.getMainLooper()).postDelayed({
                if(listFlyInfoDataTakeoff.isNotEmpty()){
                    mAdapter?.updateDataList(listFlyInfoDataTakeoff)
                }
            },500)

        }
    }

    private val rcOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 10 && !isRunningAnimation && binding.toTopFab.visibility == View.INVISIBLE) {
                translateAnimationOfFab(true)
                scrollDirectionCallBack.getScrollDirection(dy)
            } else if (!recyclerView.canScrollVertically(-1) && !isRunningAnimation && binding.toTopFab.visibility == View.VISIBLE) {
                translateAnimationOfFab(false)
                scrollDirectionCallBack.getScrollDirection(dy)
            }
        }
    }

    private fun setNightModeObserver(){
        viewModel.isNightMode.observe(viewLifecycleOwner){ isNightMode ->
            mAdapter?.getListData()?.forEach {
                (it as FlyDetailData).isNightMode = isNightMode
            }
            mAdapter?.notifyDataSetChanged()

            if(isNightMode){
                binding.toTopFab.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue_gray_night))
            }else{
                binding.toTopFab.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue_gray))
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun initClickListener() {

        binding.toTopFab.clicks()
            .throttleFirst(FlyTPEConstants.CLICK_MILLISECONDS_TIMER, TimeUnit.MILLISECONDS)
            .subscribe {
                translateAnimationOfFab(false)

                if ((mAdapter?.getListData()?.size
                        ?: 0) > 20 && (binding.rcFlyInfo.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition() > 20
                ) {
                    binding.rcFlyInfo.scrollToPosition(10)
                    binding.rcFlyInfo.smoothScrollToPosition(0)
                    scrollDirectionCallBack.getScrollDirection(-1)
                } else {
                    binding.rcFlyInfo.smoothScrollToPosition(0)
                    scrollDirectionCallBack.getScrollDirection(-1)
                }

            }
    }

    private fun translateAnimationOfFab(isEnter: Boolean) {
        val valueAnimatorVerticalBias: ValueAnimator
        if (isEnter) {
            binding.toTopFab.visibility = View.VISIBLE
            valueAnimatorVerticalBias = ValueAnimator.ofFloat(1.1f, 0.86f)
                .setDuration(300)
        } else {
            valueAnimatorVerticalBias = ValueAnimator.ofFloat(0.86f, 1.1f)
                .setDuration(300)
        }

        valueAnimatorVerticalBias.addUpdateListener { animation ->
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.clFlyInfoContainer)
            constraintSet.setVerticalBias(
                R.id.toTopFab,
                (animation.animatedValue as Float)
            )
            constraintSet.applyTo(binding.clFlyInfoContainer)
        }

        valueAnimatorVerticalBias.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isRunningAnimation = true
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!isEnter) {
                    binding.toTopFab.visibility = View.INVISIBLE
                }
                isRunningAnimation  = false
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })

        valueAnimatorVerticalBias.start()
    }
}