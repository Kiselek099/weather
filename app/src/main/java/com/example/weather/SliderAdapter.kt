package com.example.weather


import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.weather.fragments.OnBoardingFragmentViewPagerModel
import com.example.weather.fragments.SliderFragment

class SliderAdapter(
    fragment: FragmentActivity,
    private val viewPagerList: MutableList<OnBoardingFragmentViewPagerModel>
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return viewPagerList.size
    }
    override fun createFragment(position: Int): Fragment {
        val fragment = SliderFragment()
        fragment.arguments = bundleOf("vp" to viewPagerList[position])
        return fragment
    }
}
