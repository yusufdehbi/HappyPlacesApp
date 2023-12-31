package com.dehbideveloper.happyplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dehbideveloper.happyplaces.R
import com.dehbideveloper.happyplaces.adapters.HappyPlacesAdapter
import com.dehbideveloper.happyplaces.database.DatabaseHandler
import com.dehbideveloper.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.dehbideveloper.happyplaces.models.HappyPlaceModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    var binding: ActivityAddHappyPlaceBinding? = null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage : Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mHappyPlaceDetails: HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarAddPlace) // Use the toolbar to set the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // This is to use the home back button.
        // Setting the click event to the back button
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener{ view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        if (mHappyPlaceDetails != null) {
            supportActionBar?.title = "Edit Happy Place"
            binding?.etTitle?.setText(mHappyPlaceDetails!!.title)
            binding?.etDescription?.setText(mHappyPlaceDetails!!.description)
            binding?.etDate?.setText(mHappyPlaceDetails!!.date)
            binding?.etLocation?.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)

            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)

            binding?.btnSave?.text = "Update"
        }

        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
    }



    override fun onClick(v: View?) {
        when(v!!.id){
            // Handle click on the date EditText
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

            // Handle click on the add image TextView
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery",
                    "Capture photo from Camera")
                pictureDialog.setItems(pictureDialogItems){
                    _, which ->
                    when(which){
                        // Trigger the method to choose a photo from the Gallery
                        0 -> choosePhotoFromGallery()
                        // Trigger the method to take a photo from the Camera
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            // Handle saving happy place data into database
            R.id.btn_save -> {
                when {
                    binding?.etTitle?.text!!.isBlank() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter title", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etDescription?.text!!.isBlank() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter description", Toast.LENGTH_SHORT).show()
                    }
                    binding?.etLocation?.text!!.isBlank() -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter a location", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this@AddHappyPlaceActivity, "Please enter an image", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        val happyPlaceModel = HappyPlaceModel(
                            mHappyPlaceDetails?.id ?: 0,
                            binding?.etTitle?.text!!.toString(),
                            saveImageToInternalStorage.toString(),
                            binding?.etDescription?.text!!.toString(),
                            binding?.etDate?.text!!.toString(),
                            binding?.etLocation?.text!!.toString(),
                            mLatitude,
                            mLongitude
                        )

                        val dbHandler = DatabaseHandler(this)
                        if (mHappyPlaceDetails == null){
                            val addHappyPlaceResult = dbHandler.addHappyPlace(happyPlaceModel)
                            if (addHappyPlaceResult > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            val updateHappyPlaceResult = dbHandler.updateHappyPlace(happyPlaceModel)
                            if (updateHappyPlaceResult > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }



                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("SAVED IMAGE", "PATH: $saveImageToInternalStorage")
                        binding?.ivPlaceImage?.setImageBitmap(selectedImageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@AddHappyPlaceActivity,
                            "Failed to load image form gallery",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            else if (requestCode == CAMERA){
                val thumbnail: Bitmap = data?.extras?.get("data") as Bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("SAVED IMAGE", "PATH: $saveImageToInternalStorage")
                binding?.ivPlaceImage?.setImageBitmap(thumbnail)
            }
        }
    }


    private fun takePhotoFromCamera(){
        // Use the Dexter library to request runtime permissions for reading and writing external storage and camera.
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()){
                    // All required permissions are granted, show a toast message.
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                permissionToken: PermissionToken?
            ) {
                // Display a rationale dialog to explain the need for permissions.
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun choosePhotoFromGallery(){
        // Use the Dexter library to request runtime permissions for reading and writing external storage.
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()){
                    // All required permissions are granted, show a toast message.
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                permissionToken: PermissionToken?
            ) {
                // Display a rationale dialog to explain the need for permissions.
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    //Redirect User to the Setting to grand the application access
    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("It looks like you turned of permission required for this features it can" +
                    " be enabled under the application settings")
            .setPositiveButton("Go To Settings"){
                _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){
                dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    //put the date in the date EditText
    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,stream )
            stream.flush()
            stream.close()
        } catch (e: IOException){
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA  = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }
}