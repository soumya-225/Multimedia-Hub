package com.example.multimediahubviews

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.ArrayList


class AdapterViewPager(fragmentActivity: FragmentActivity, private var arr: ArrayList<Fragment>) : FragmentStateAdapter(fragmentActivity) {
    init {
        this.arr = arr
    }

    override fun getItemCount(): Int {
        return arr.size
    }

    override fun createFragment(position: Int): Fragment {
        return arr[position]
    }
}