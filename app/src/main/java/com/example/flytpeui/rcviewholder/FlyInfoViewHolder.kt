package com.example.flytpeui.rcviewholder

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.base.CustomApplication
import com.example.base.FlyTPEConstants
import com.example.flytpe.R
import com.example.model.FlyDetailData

class FlyInfoViewHolder(itemView: View): BaseViewHolder<Any>(itemView) {

    private val container = itemView.findViewById<ConstraintLayout>(R.id.clFlyInfoDetailContainer)
    private val vFromTo = itemView.findViewById<View>(R.id.viewFromTo)
    private val flyInfoTypeIcon = itemView.findViewById<ImageView>(R.id.imItemFlyInfoTypeIcon)
    private val tv01 = itemView.findViewById<ConstraintLayout>(R.id.tvSetOrderTime)
    private val tv02 = itemView.findViewById<ConstraintLayout>(R.id.tvSetRealTime)
    private val tv03 = itemView.findViewById<ConstraintLayout>(R.id.tvSetAirPortFrom)
    private val tv04 = itemView.findViewById<ConstraintLayout>(R.id.tvSetFlyLineInfo)
    private val tv05 = itemView.findViewById<ConstraintLayout>(R.id.tvSetAirPortArrival)
    private val tv06 = itemView.findViewById<TextView>(R.id.tvFlyPlaneCurCondition)
    private val tv01InsiderUp = tv01.findViewById<TextView>(R.id.txInfoUp)
    private val tv01InsiderDw = tv01.findViewById<TextView>(R.id.tvInfoDown)
    private val tv02InsiderUp = tv02.findViewById<TextView>(R.id.txInfoUp)
    private val tv02InsiderDw = tv02.findViewById<TextView>(R.id.tvInfoDown)
    private val tv03InsiderUp = tv03.findViewById<TextView>(R.id.txInfoUp)
    private val tv03InsiderDw = tv03.findViewById<TextView>(R.id.tvInfoDown)
    private val tv04InsiderUp = tv04.findViewById<TextView>(R.id.txInfoUp)
    private val tv04InsiderDw = tv04.findViewById<TextView>(R.id.tvInfoDown)
    private val tv05InsiderUp = tv05.findViewById<TextView>(R.id.txInfoUp)
    private val tv05InsiderDw = tv05.findViewById<TextView>(R.id.tvInfoDown)
    private val imItemFlyInfoTypeIcon = itemView.findViewById<ImageView>(R.id.imItemFlyInfoTypeIcon)

    @SuppressLint("SetTextI18n")
    override fun bindViewData(data: Any, position:Int) {
        val mData = data as FlyDetailData
        val tvUpViewList = listOf<TextView>(
            tv01InsiderUp,tv02InsiderUp,tv03InsiderUp,tv04InsiderUp,tv05InsiderUp)
        val tvDwViewList = listOf<TextView>(
            tv01InsiderDw,tv02InsiderDw,tv03InsiderDw,tv04InsiderDw,tv05InsiderDw)
        if(mData.isFake){
            imItemFlyInfoTypeIcon.visibility = View.INVISIBLE
            tvUpViewList.forEach {
                it.visibility = View.INVISIBLE
            }
            tvDwViewList.forEach {
                it.visibility = View.INVISIBLE
            }
        }else{
            tvUpViewList.forEach {
                it.visibility = View.VISIBLE
            }
            tvDwViewList.forEach {
                it.visibility = View.VISIBLE
            }
        }

        if(mData.isNightMode){
            container.setBackgroundResource(R.drawable.fly_info_item_background_night)
            Color.WHITE.let {
                tvDwViewList.forEach { view ->
                    view.setTextColor(it)
                }
                tvUpViewList.forEach { view ->
                    view.setTextColor(it)
                }
            }
            vFromTo.setBackgroundColor(Color.WHITE)
        }else{
            container.setBackgroundResource(R.drawable.fly_info_item_background)
            Color.BLACK.let {
                tvDwViewList.forEach { view ->
                    view.setTextColor(it)
                }
                tvUpViewList.forEach { view ->
                    view.setTextColor(it)
                }
            }
            vFromTo.setBackgroundColor(Color.BLACK)
        }

        if(mData.flyType == FlyTPEConstants.ARRIVAL_FLY){
            if(mData.isNightMode){
                flyInfoTypeIcon.setImageResource(R.drawable.land_white)
            }else{
                flyInfoTypeIcon.setImageResource(R.drawable.land_black)
            }
        }else{
            if(mData.isNightMode){
                flyInfoTypeIcon.setImageResource(R.drawable.takeoff_white)
            }else{
                flyInfoTypeIcon.setImageResource(R.drawable.takeoff_black)
            }
        }

        tvUpViewList[0].text = tvUpViewList[0].context.getString(R.string.OrderTime)
        tvUpViewList[1].text = tvUpViewList[1].context.getString(R.string.RealTime)
        tvUpViewList[2].text = mData.departureAirportID
        tvUpViewList[3].text = tvUpViewList[1].context.getString(R.string.FlyPlaneNumber,mData.flightNumber)
        tvUpViewList[4].text = mData.arrivalAirportID

        tvDwViewList[0].text = mData.scheduleTime
        tvDwViewList[1].text = mData.actualTime
        tvDwViewList[2].text = mData.departureAirport.trim()
        tvDwViewList[4].text = mData.arrivalAirport.trim()
        if(!mData.isFake){
            vFromTo.visibility = View.VISIBLE
            val formattedTerminal = String.format("%02d",mData.terminal.toInt())
            val checkGate = if(mData.gate.isNullOrEmpty()) "－－" else mData.gate
            tvDwViewList[3].text = tvDwViewList[3].context.getString(R.string.FlyPlaneGateNumber,formattedTerminal,checkGate)
            imItemFlyInfoTypeIcon.visibility = View.VISIBLE
        }

        when(mData.remark){
            "已到ARRIVED"->{
                var frontChars = ""
                var backChard = ""
                mData.remark.forEachIndexed { index, c ->
                    if(index>1){
                        backChard += c
                    }else{
                        frontChars += c
                    }
                }
                tv06.text = "$frontChars::$backChard"
                tv06.setTextColor(Color.GREEN)
            }
            "出發DEPARTED"->{
                var frontChars = ""
                var backChard = ""
                mData.remark.forEachIndexed { index, c ->
                    if(index>1){
                        backChard += c
                    }else{
                        frontChars += c
                    }
                }
                tv06.text = "$frontChars::$backChard"
                tv06.setTextColor(Color.BLACK)
            }
            "取消CANCELLED"->{
                var frontChars = ""
                var backChard = ""
                mData.remark.forEachIndexed { index, c ->
                    if(index>1){
                        backChard += c
                    }else{
                        frontChars += c
                    }
                }
                tv06.text = "$frontChars::$backChard"
                tv06.setTextColor(Color.RED)
            }
            "時間更改SCHEDULE CHANGE"->{
                var frontChars = ""
                var backChard = ""
                mData.remark.forEachIndexed { index, c ->
                    if(index>3){
                        backChard += c
                    }else{
                        frontChars += c
                    }
                }
                tv06.text = "$frontChars::\n$backChard"
                tv06.setTextColor(Color.YELLOW)
            }
        }
    }
}
