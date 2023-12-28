package com.ramon.infogo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ramon.infogo.databinding.ActivityMainBinding
import com.ramon.infogo.databinding.ActivityMainSliderBinding

class MainSlider : AppCompatActivity() {

    private val TAG = "RMN"
    private lateinit var binding: ActivityMainSliderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainSliderBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}