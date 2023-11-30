package com.coderhymes.pdfaura.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coderhymes.pdfaura.R
import com.coderhymes.pdfaura.adapter.PDFAdapter
import java.io.File
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AllFile.newInstance] factory method to
 * create an instance of this fragment.
 */
class AllFile : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var pdfAdapter: PDFAdapter
    private var pdfList: MutableList<PdfModel> = mutableListOf()

    // ... (rest of your code)
    private val STORAGE_PERMISSION_CODE = 23
    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android is 11 (R) or above
                if (Environment.isExternalStorageManager()) {
                    // Manage External Storage Permissions Granted
                    Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted")
                } else {
                    Toast.makeText(requireContext(), "Storage Permissions Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_file, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val pdfList = getPdfList() ?: listOf()
        pdfAdapter = PDFAdapter(requireContext(), pdfList.toMutableList())
        recyclerView.adapter = pdfAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!checkStoragePermissions()) {
            requestForStoragePermissions()
        }
    }

    private fun getPdfList(): List<PdfModel>? {
        val myList: MutableList<PdfModel> = ArrayList()
        val selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?"
        val pdfMimeType = "application/pdf"
        val selectionArgs = arrayOf(pdfMimeType)
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED // Add modification date column
        )

        requireActivity().contentResolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
            val dataIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            val displayNameIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
            val dateModifiedIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val filePath = if (dataIndex != -1) cursor.getString(dataIndex) else ""
                val fileName = if (displayNameIndex != -1) cursor.getString(displayNameIndex) else ""
                val fileSize = if (sizeIndex != -1) cursor.getLong(sizeIndex).toString() else ""
                val dateModified = if (dateModifiedIndex != -1) cursor.getLong(dateModifiedIndex) * 1000 else 0L

                if (filePath.isNotEmpty() && fileName.isNotEmpty()) {
                    val file = File(filePath)
                    val pdfModel = PdfModel(fileName, fileSize, 0, "pdf", file.path, Date(dateModified))
                    myList.add(pdfModel)
                }
            }
        }

//        if (myList.isEmpty()) {
//            Log.d("DataCheck", "Data is empty")
//        } else {
//            Log.d("DataCheck", "Data is not null: $myList")
//        }

        return myList
    }


    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or above
            Environment.isExternalStorageManager()
        } else {
            // Below Android 11
            val writePermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val readPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)

            readPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestForStoragePermissions() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Storage Permission Required")
        builder.setMessage("Please grant the storage permission to access files.")
        builder.setPositiveButton("Grant Permission") { dialog, which ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                    val uri = Uri.fromParts("package", requireActivity().packageName, null)
                    intent.data = uri
                    storageActivityResultLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to open permission settings", Toast.LENGTH_SHORT).show()
                }
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }




    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED

                if (read && write) {
                    Toast.makeText(requireContext(), "Storage Permissions Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Storage Permissions Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        pdfList.clear()
        pdfList.addAll(getPdfList()?.sortedByDescending { it.fileDate }?.toMutableList() ?: mutableListOf())
        pdfAdapter.updateList(pdfList)
    }

    companion object {
        private const val TAG = "HomeFragment"
    }


}