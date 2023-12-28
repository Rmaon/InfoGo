package com.ramon.infogo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ramon.infogo.databinding.ActivityMainSliderBinding
import androidx.viewpager.widget.ViewPager

class MainSlider : AppCompatActivity() {

    private val TAG = "RMN"
    private lateinit var binding: ActivityMainSliderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainSliderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.Toolbar)

        // Configurar el ViewPager con el adaptador
        val viewPager: ViewPager = findViewById(R.id.idViewPager)
        val adapter = ViewPagerAdapter(supportFragmentManager)  // Cambiado a IncidenciaPagerAdapter
        viewPager.adapter = adapter
    }
}
