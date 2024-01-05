package com.coderhymes.pdfaura.fragment

import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.coderhymes.pdfaura.R
import com.coderhymes.pdfaura.adapter.SelectedImagesAdapter
import com.coderhymes.pdfaura.database.AppDatabase
import com.coderhymes.pdfaura.database.ImageEntity
import com.coderhymes.pdfaura.databinding.FragmentCreatePdfBinding
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreatePDF : Fragment() {

    private lateinit var binding: FragmentCreatePdfBinding
    private lateinit var oneIV: ImageView
    private lateinit var twoIV: ImageView
    private lateinit var threeIV: ImageView
    private lateinit var fourIV: ImageView
    private lateinit var fiveIV: ImageView
    private lateinit var sixIV: ImageView

    private lateinit var bmp: Bitmap
//    private lateinit var onebmp: Bitmap
//    private lateinit var twobmp: Bitmap
//    private lateinit var threebmp: Bitmap
//    private lateinit var fourbmp: Bitmap
//    private lateinit var fivebmp: Bitmap
//    private lateinit var sixbmp: Bitmap

    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var db: AppDatabase

    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
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

        initializeImageViews()
        pickMultipleMedia()
        binding.floatingActionButton.setOnClickListener {
            pickMultipleMedia()
        }
        geAllMedia()
    }

    private fun initializeImageViews() {
        oneIV = binding.idIVOne
        twoIV = binding.idIVTwo
        threeIV = binding.idIVThree
        fourIV = binding.idIVFour
        fiveIV = binding.idIVFive
        sixIV = binding.idIVSix

        setFilterOnClickListeners()
    }

    private fun pickMultipleMedia() {
        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun handleSelectedMedia(uris: List<Uri>) {
        lifecycleScope.launch(Dispatchers.IO) {
            selectedImageUris.clear()
            selectedImageUris.addAll(uris)

            db = Room.databaseBuilder(
                requireContext(),
                AppDatabase::class.java, "omgimages"
            ).build()

            val imageDao = db.imageDao()

            val images = selectedImageUris.map { uri ->
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(uri, flag)
                ImageEntity(uri = uri.toString())
            }

            try {
                for (image in images) {
                    val existingImage = imageDao.findByName(image.uri!!)
                    if (existingImage == null) {
                        imageDao.insert(image)
                    } else {
                        image.apply {
                            Iid = existingImage.Iid
                        }
                        imageDao.update(image)
                    }
                }
                geAllMedia()

            } catch (e: SQLiteConstraintException) {
                Log.e("SQLiteException", "Error inserting/updating data: ${e.message}")
            } finally {
                db.close()
            }
        }
    }

    private fun getAllImagesFromDatabase(): List<ImageEntity> {
        if (!::db.isInitialized) {
            db = Room.databaseBuilder(
                requireContext(),
                AppDatabase::class.java, "omgimages"
            ).build()
        }
        return db.imageDao().getAll()
    }

    private fun geAllMedia() {
        lifecycleScope.launch(Dispatchers.IO) {
            val imagesFromDatabase = getAllImagesFromDatabase()
            withContext(Dispatchers.Main) {
                displayImages(imagesFromDatabase)
            }
        }
    }

    private fun displayImages(imagesFromDatabase: List<ImageEntity>) {
        val recyclerView: RecyclerView = binding.imgRecyclerView
        val imageAdapter = SelectedImagesAdapter(images = imagesFromDatabase)
        recyclerView.adapter = imageAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setFilterOnClickListeners() {
        val documentFilter = DocumentFilter()
        bmp = BitmapFactory.decodeResource(resources, R.drawable.dog)

        oneIV.setOnClickListener {
            onFilterSelected(0)
        }
        documentFilter.getGreyScaleFilter(bmp) { greyScaledBitmap ->
            twoIV.setImageBitmap(greyScaledBitmap)
            twoIV.setOnClickListener {
                onFilterSelected(1) // Pass the filter index to the listener
            }
        }

        documentFilter.getBlackAndWhiteFilter(bmp) { blackAndWhite ->
            threeIV.setImageBitmap(blackAndWhite)
            threeIV.setOnClickListener {
                onFilterSelected(2) // Pass the filter index to the listener
            }
        }

        documentFilter.getShadowRemoval(bmp) { getShadowRemoval ->
            sixIV.setImageBitmap(getShadowRemoval)
            sixIV.setOnClickListener {
                onFilterSelected(3) // Pass the filter index to the listener
            }
        }

        documentFilter.getMagicFilter(bmp) { getMagicFilter ->
            fourIV.setImageBitmap(getMagicFilter)
            fourIV.setOnClickListener {
                onFilterSelected(4) // Pass the filter index to the listener
            }
        }

        documentFilter.getLightenFilter(bmp) { getShadowRemoval ->
            fiveIV.setImageBitmap(getShadowRemoval)
            fiveIV.setOnClickListener {
                onFilterSelected(5) // Pass the filter index to the listener
            }
        }
    }

    private fun onFilterSelected(i: Int) {

    }
}
