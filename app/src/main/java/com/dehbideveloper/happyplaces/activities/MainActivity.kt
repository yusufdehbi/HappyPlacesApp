package com.dehbideveloper.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dehbideveloper.happyplaces.adapters.HappyPlacesAdapter
import com.dehbideveloper.happyplaces.database.DatabaseHandler
import com.dehbideveloper.happyplaces.databinding.ActivityMainBinding
import com.dehbideveloper.happyplaces.models.HappyPlaceModel
import com.happyplaces.utils.SwipeToDeleteCallback
import pl.kitek.rvswipetodelete.SwipeToEditCallback

class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddHappyPlace?.setOnClickListener{
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getHappyPlacesFromLocalDB()
    }

    private fun setupHappyPlacesRecyclerView(happyPlaceList: ArrayList<HappyPlaceModel>){
        binding?.rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)
        binding?.rvHappyPlacesList?.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(this, happyPlaceList)
        binding?.rvHappyPlacesList?.adapter = placesAdapter

        placesAdapter.setOnClickListener(object : HappyPlacesAdapter.OnClickListener {
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvHappyPlacesList?.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvHappyPlacesList?.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getHappyPlacesFromLocalDB()
            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)
    }

    private fun getHappyPlacesFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val getHappyPlacesList: ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if (getHappyPlacesList.size > 0){
            binding?.rvHappyPlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
            setupHappyPlacesRecyclerView(getHappyPlacesList)
        } else {
            binding?.rvHappyPlacesList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                getHappyPlacesFromLocalDB()
            } else {
                Log.e("Activitty", "Cancelled or Back Pressed")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}