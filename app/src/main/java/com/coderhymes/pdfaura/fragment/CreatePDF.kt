package com.coderhymes.pdfaura.fragment

import android.database.sqlite.SQLiteConstraintException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.coderhymes.pdfaura.R
import com.coderhymes.pdfaura.adapter.SelectedImagesAdapter
import com.coderhymes.pdfaura.database.AppDatabase
import com.coderhymes.pdfaura.database.ImageEntity
import com.coderhymes.pdfaura.databinding.FragmentCreatePdfBinding
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatePDF : Fragment() {

    private lateinit var adapter: SelectedImagesAdapter
    private lateinit var binding: FragmentCreatePdfBinding
    private lateinit var oneIV: ImageView
    private lateinit var twoIV: ImageView
    private lateinit var threeIV: ImageView
    private lateinit var fourIV: ImageView
    private lateinit var fiveIV: ImageView
    private lateinit var sixIV: ImageView
    private lateinit var originalIV: ImageView

    private lateinit var bmp: Bitmap
    private lateinit var onebmp: Bitmap
    private lateinit var twobmp: Bitmap
    private lateinit var threebmp: Bitmap
    private lateinit var fourbmp: Bitmap
    private lateinit var fivebmp: Bitmap
    private lateinit var sixbmp: Bitmap

    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var db: AppDatabase

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            // Callback is invoked after the user selects media items or closes the
            // photo picker.
            if (uris.isNotEmpty()) {
                Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
                handleSelectedMedia(uris)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatePdfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SelectedImagesAdapter(selectedImageUris) { }
        binding.imgRecyclerView.adapter = adapter

        initializeImageViews()

        binding.floatingActionButton.setOnClickListener {
            pickMultipleMedia()
        }

        setFilterOnClickListeners()
    }

    private fun initializeImageViews() {
        oneIV = binding.idIVOne
        twoIV = binding.idIVTwo
        threeIV = binding.idIVThree
        fourIV = binding.idIVFour
        fiveIV = binding.idIVFive
        sixIV = binding.idIVSix
//        originalIV = binding.idIVOriginalImage
    }

    private fun pickMultipleMedia() {
        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun handleSelectedMedia(uris: List<Uri>) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Clear existing selected image URIs and add new ones
            selectedImageUris.clear()
            selectedImageUris.addAll(uris)

            // Create and initialize the database instance
            db = Room.databaseBuilder(
                requireContext(),
                AppDatabase::class.java, "omgimages"
            ).build()

            // Get image DAO
            val imageDao = db.imageDao()

            // Convert URIs to ImageEntity objects
            val images = selectedImageUris.map { uri ->
                ImageEntity(uri = uri.toString())
            }

            try {
                // Loop through the images and insert or update them
                for (image in images) {
                    val existingImage = imageDao.findByName(image.uri!!)
                    if (existingImage == null) {
                        // If the image doesn't exist, insert it
                        imageDao.insert(image)
                    } else {
                        // If the image already exists, update its data
                        image.apply {
                            Iid = existingImage.Iid // Ensure the primary key is retained
                        }
                        imageDao.update(image)
                    }
                }
                logAllImages()

            } catch (e: SQLiteConstraintException) {
                // Log the exception and handle it appropriately
                Log.e("SQLiteException", "Error inserting/updating data: ${e.message}")
                // Additional error handling code goes here
            }

            db.close()
        }
    }


    private fun logAllImages() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Check if db is not null before accessing it
            if (db != null) {
                var images = db!!.imageDao().getAll()
                for (image in images) {
                    Log.d("ImageEntity", "Id: ${image.Iid}, Uri: ${image.uri}")
                }
                adapter.updateData(images)
            } else {
                Log.e("CreatePDF", "Error: db is null")
            }
        }
    }



    private fun setFilterOnClickListeners() {
        val documentFilter = DocumentFilter()
        bmp = BitmapFactory.decodeResource(resources, R.drawable.dog)

        documentFilter.getGreyScaleFilter(bmp) { greyScaledBitmap ->
            twoIV.setImageBitmap(greyScaledBitmap)
        }

        documentFilter.getBlackAndWhiteFilter(bmp) { blackAndWhite ->
            threeIV.setImageBitmap(blackAndWhite)
        }

        documentFilter.getShadowRemoval(bmp) { getShadowRemoval ->
            sixIV.setImageBitmap(getShadowRemoval)
        }

        documentFilter.getMagicFilter(bmp) { getMagicFilter ->
            fourIV.setImageBitmap(getMagicFilter)
        }

        documentFilter.getLightenFilter(bmp) { getShadowRemoval ->
            fiveIV.setImageBitmap(getShadowRemoval)
        }

        // Set original image click listeners
        setOriginalImageClickListeners()
    }

    private fun setOriginalImageClickListeners() {
        oneIV.setOnClickListener { originalIV.setImageBitmap(onebmp) }
        twoIV.setOnClickListener { originalIV.setImageBitmap(twobmp) }
        threeIV.setOnClickListener { originalIV.setImageBitmap(threebmp) }
        fourIV.setOnClickListener { originalIV.setImageBitmap(fourbmp) }
        fiveIV.setOnClickListener { originalIV.setImageBitmap(fivebmp) }
        sixIV.setOnClickListener { originalIV.setImageBitmap(sixbmp) }
    }
}
