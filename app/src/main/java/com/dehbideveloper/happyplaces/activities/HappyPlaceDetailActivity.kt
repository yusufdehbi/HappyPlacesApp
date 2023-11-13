package com.dehbideveloper.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dehbideveloper.happyplaces.R
import com.dehbideveloper.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.dehbideveloper.happyplaces.databinding.ActivityMainBinding

class HappyPlaceDetailActivity : AppCompatActivity() {

    var binding: ActivityHappyPlaceDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}