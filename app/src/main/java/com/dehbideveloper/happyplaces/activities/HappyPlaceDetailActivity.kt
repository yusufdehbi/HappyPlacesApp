package com.dehbideveloper.happyplaces.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dehbideveloper.happyplaces.R
import com.dehbideveloper.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.dehbideveloper.happyplaces.databinding.ActivityMainBinding
import com.dehbideveloper.happyplaces.models.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {

    var binding: ActivityHappyPlaceDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var happyPlaceDetailModel: HappyPlaceModel? = null

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailModel = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        if (happyPlaceDetailModel != null){
            setSupportActionBar(binding?.toolbarHappyPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.title

            binding?.toolbarHappyPlaceDetail?.setNavigationOnClickListener {
                onBackPressed()
            }

            binding?.ivPlaceImage?.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            binding?.tvDescription?.text = happyPlaceDetailModel.description
            binding?.tvLocation?.text = happyPlaceDetailModel.location
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}