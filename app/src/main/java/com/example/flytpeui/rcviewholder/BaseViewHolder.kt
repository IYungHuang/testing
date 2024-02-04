package com.example.flytpeui.rcviewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<Any>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bindViewData(data: kotlin.Any,position:Int)
}