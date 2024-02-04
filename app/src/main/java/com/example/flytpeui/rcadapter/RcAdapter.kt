package com.example.flytpeui.rcadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flytpeui.rcviewholder.BaseViewHolder
import com.example.flytpeui.rcviewholder.FlyInfoViewHolder
import com.example.model.IItemLayoutRes

class RcAdapter(private var dataList: List<Any>?):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mdatalist = mutableListOf<Any>()

    init {
        dataList?.forEach {
            mdatalist.add(it)
        }
    }

    fun getListData(): List<Any> {
        return mdatalist
    }

    fun updateDataList(updateDataList: List<Any>?) {
        mdatalist.clear()
        updateDataList?.forEach {
            mdatalist.add(it)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder : RecyclerView.ViewHolder

        val view : View = LayoutInflater.from(parent.context).inflate(
            viewType, parent, false
        )
        viewHolder = FlyInfoViewHolder(view)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return mdatalist.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mHolder = (holder as? BaseViewHolder<*>) ?: return
        mHolder.bindViewData(mdatalist[position],position)
    }

    override fun getItemViewType(position: Int): Int {
        return (mdatalist.get(position) as? IItemLayoutRes)?.mtype ?: -1
    }
}