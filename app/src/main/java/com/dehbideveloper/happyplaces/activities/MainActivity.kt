package com.dehbideveloper.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.dehbideveloper.happyplaces.database.DatabaseHandler
import com.dehbideveloper.happyplaces.databinding.ActivityMainBinding
import com.dehbideveloper.happyplaces.models.HappyPlaceModel

class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener{
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }

        getHappyPlacesFromLocalDB()
    }

    private fun getHappyPlacesFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val getHappyPlacesList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if (getHappyPlacesList.size > 0){
            for (i in getHappyPlacesList){
                Log.e("09876YY Title", i.title)
                Log.e("09876YY Description", i.description)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}