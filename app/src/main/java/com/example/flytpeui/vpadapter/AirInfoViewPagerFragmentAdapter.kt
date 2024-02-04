package com.example.flytpeui.vpadapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class AirInfoViewPagerFragmentAdapter(private val pageViewList: MutableList<Fragment>, val fragment: Fragment) : FragmentStateAdapter(fragment) {

    private var currentPosition = 0

    override fun getItemCount(): Int {
        return pageViewList.size
    }

    override fun createFragment(position: Int): Fragment {
        currentPosition = position
        return pageViewList[position]
    }

}