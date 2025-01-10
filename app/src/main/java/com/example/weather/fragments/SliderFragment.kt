package com.example.weather.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.example.weather.R
import com.example.weather.WeatherActivity

class SliderFragment : Fragment() {
    private var check = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_slider, container, false)
        val startBTN: AppCompatButton = view.findViewById(R.id.startBTN)
        val imageIV: ImageView = view.findViewById(R.id.imageIV)

        val viewPagerItem = arguments?.getSerializable("vp") as? OnBoardingFragmentViewPagerModel
            ?: return view

        check = viewPagerItem.checkButton ?: true
        imageIV.setImageResource(viewPagerItem.imageView ?: R.drawable.ic_placeholder)

        if (!check) {
            startBTN.visibility = View.VISIBLE
            startBTN.setOnClickListener {
                startActivity(Intent(activity, WeatherActivity::class.java))
            }
        } else {
            startBTN.visibility = View.GONE
        }
        return view
    }
}