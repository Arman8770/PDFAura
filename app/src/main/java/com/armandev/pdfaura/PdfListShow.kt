package com.armandev.pdfaura

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


@Suppress("DEPRECATION")
class PdfListShow : AppCompatActivity(){

    private lateinit var recyclerView: RecyclerView
    lateinit var adView: AdView
    private lateinit var searchView:SearchView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_list_show)
        supportActionBar!!.hide()
        recyclerView = findViewById(R.id.rView)

        //For search
        searchView = findViewById(R.id.searchView)
        searchView.clearFocus()

        //admob test
        adView = findViewById(R.id.adView)
        requestReadExternalStoragePermission()
        MobileAds.initialize(this)
        banner()

    }



    fun banner(){
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.adListener= object : AdListener(){
            override fun onAdClicked() {
                Log.d(TAG,"onAdClicked: ")
            }

            override fun onAdClosed() {
                Log.d(TAG,"onAdClosed: ")
            }

            override fun onAdImpression() {
                Log.d(TAG,"onAdImpression: ")
            }

            override fun onAdLoaded() {
                Log.d(TAG,"onAdLoaded: ")
            }

            override fun onAdOpened() {
                Log.d(TAG,"onAdOpened: ")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                Log.d(TAG,"onAdFailed: " +p0.message)
            }
        }

    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Permission Granted for android 11+
                        loadAllFilesToDatabase()
                    } else {
                        showAndroid11PlusPermissionDialog()
                    }
                } else {
                    // Permission Granted for android marshmallow+
                    loadAllFilesToDatabase()
                }
            } else {
                // Permission not granted
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private val android11PlusSettingResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Environment.isExternalStorageManager()) {
                // Permission Granted for android 11+
                loadAllFilesToDatabase()
            } else {
                // Permission not granted
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
    fun requestReadExternalStoragePermission() {
        when {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Permission Granted for android 11+
                        loadAllFilesToDatabase()
                    } else {
                        showAndroid11PlusPermissionDialog()
                    }
                } else {
                    // Permission Granted
                    loadAllFilesToDatabase()
                }
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Permission Granted
                        loadAllFilesToDatabase()
                        return
                    }
                }
                // show rational dialog
            }
            else -> {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ).toString()
                    )
                } else {
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun showAndroid11PlusPermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(this.getString(R.string.allow_access))
            .setMessage(this.getString(R.string.allow_access_detail))
            .setPositiveButton(this.getString(R.string.open_setting)) { dialog, _ ->
                val intent = Intent().apply {
                    action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    data = Uri.fromParts("package", this@PdfListShow.packageName, null)
                }
                dialog.dismiss()
                android11PlusSettingResultLauncher.launch(intent)
            }
            .setNegativeButton(this.getString(R.string.not_now)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun checkPermission(permission: String) =
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    private fun getAllMediaFilesCursor(): Cursor? {

        val projections =
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA, //TODO: Use URI instead of this.. see official docs for this field
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE
            )

        val sortBy = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"

        val selectionArgs =
            FileTypes.values().map { it.mimeTypes }.flatten().filterNotNull().toTypedArray()

        val args = selectionArgs.joinToString {
            "?"
        }

        val selection =
            MediaStore.Files.FileColumns.MIME_TYPE + " IN (" + args + ")"

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        return this.contentResolver.query(
            collection,
            projections,
            selection,
            selectionArgs,
            sortBy
        )
    }

    private fun loadAllFilesToDatabase() {
        val cursor = getAllMediaFilesCursor()
        val tempList = ArrayList<PDFdata>()

        if (true == cursor?.moveToFirst()) {
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val pathCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            val nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
            val mimeType = cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE)
            val sizeCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)

            do {
                val id = cursor.getLong(idCol)
                val path = cursor.getStringOrNull(pathCol) ?: continue
                val name = cursor.getStringOrNull(nameCol) ?: continue
                val dateTime = cursor.getLongOrNull(dateCol) ?: continue
                val type = cursor.getStringOrNull(mimeType) ?: continue
                val size = cursor.getLongOrNull(sizeCol) ?: continue

                try {
                    val file= File(path)
                    if (file.exists()){
                        val Furi = Uri.fromFile(file)
                        val allFile = PDFdata(title = name, id = id,
                            date = dateTime, type = type, size = size, pdfUri = Furi, path = path)
                        tempList.add(allFile)
                    }
                }catch (e:Exception){

                }
            } while (cursor.moveToNext())
        }
        cursor?.close()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(10)
        recyclerView.adapter = PdfViewAdapter(tempList)
    }
}