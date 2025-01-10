package com.example.weather.fragments

import com.example.weather.R
import java.io.Serializable

class OnBoardingFragmentViewPagerModel(
val imageView: Int?,
val checkButton: Boolean?
) : Serializable {
    companion object {
        val viewPagerList = mutableListOf(
            OnBoardingFragmentViewPagerModel(
                R.drawable.picone,
                true
            ),
            OnBoardingFragmentViewPagerModel(
                R.drawable.picfo,
                true
            ),
            OnBoardingFragmentViewPagerModel(
                R.drawable.picthree,
                false
            )
        )
    }
}