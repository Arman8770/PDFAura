package com.coderhymes.pdfaura.adapter

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.coderhymes.pdfaura.R
import com.coderhymes.pdfaura.database.ImageEntity
import com.namangarg.androiddocumentscannerandfilter.DocumentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SelectedImagesAdapter(
    private var images: List<ImageEntity>,
    private var context: Context,
    private var filterIndex: Int,
    private var makePdf: Boolean,
) : RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder>() {
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    init {
        this.context = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_view, parent, false)
        return ImageViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilterIndex(index: Int) {
        filterIndex = index
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMakePDF(b:Boolean){
        makePdf = b
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Log.d("ImageAdapter", "onBindViewHolder called for position: $position")
        Log.d("filterListner", "onBindViewHolder filterPosition: $filterIndex + $makePdf")

        Log.d("makePDF", "is Active ${makePdf}")
        // Ensure position is within bounds
        if (position < 0 || position >= images.size) {
            return
        }
        val imageUri = images[position].uri
        val documentFilter = DocumentFilter()

        // Load image from URI using a background thread
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = loadBitmapFromUri(Uri.parse(imageUri), holder.imageView.context)

            // Switch to the main thread to update the UI
            withContext(Dispatchers.Main) {

                // Ensure the view holder still corresponds to the correct position
                if (holder.adapterPosition == position && filterIndex == 0) {
                    holder.imageView.setImageBitmap(bitmap)
                }
                else if(holder.adapterPosition == position && filterIndex == 1){
                    documentFilter.getGreyScaleFilter(bitmap) { greyScaledBitmap ->
                        holder.imageView.setImageBitmap(greyScaledBitmap)
                    }
                }
                else if(holder.adapterPosition == position && filterIndex == 2){
                    documentFilter.getBlackAndWhiteFilter(bitmap) { getBlackAndWhiteFilter ->
                        holder.imageView.setImageBitmap(getBlackAndWhiteFilter)
                    }
                }
                else if(holder.adapterPosition == position && filterIndex == 3){
                    documentFilter.getMagicFilter(bitmap) { getMagicFilter ->
                        holder.imageView.setImageBitmap(getMagicFilter)
                    }
                }
                else if(holder.adapterPosition == position && filterIndex == 4){
                    documentFilter.getLightenFilter(bitmap) { getLightenFilter ->
                        holder.imageView.setImageBitmap(getLightenFilter)
                    }
                }
                else if(holder.adapterPosition == position && filterIndex == 5){
                    documentFilter.getShadowRemoval(bitmap) { getShadowRemoval ->
                        holder.imageView.setImageBitmap(getShadowRemoval)
                    }
                }

                if (makePdf) {
                    Log.d("makePDF", "is Active ${makePdf}")

                    val progressDialog = ProgressDialog(context)
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                    progressDialog.setTitle("Creating PDF")
                    progressDialog.setCancelable(false)
                    progressDialog.max = 100

                    var document: PdfDocument? = null

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            document = PdfDocument()

                            for (i in 0..100) {
                                withContext(Dispatchers.Main) {
                                    progressDialog.progress = i
                                }
                                delay(50)
                            }

                            for (i in images.indices) {
                                val bitmap = loadBitmapFromUri(Uri.parse(images[i].uri), context)
                                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, i + 1).create()
                                val page = document!!.startPage(pageInfo)
                                val canvas = page.canvas
                                canvas.drawBitmap(bitmap, 0f, 0f, null)
                                document!!.finishPage(page)
                            }

                            val pdfFile = File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                                "bitmap_to_pdf.pdf"
                            )

                            withContext(Dispatchers.IO) {
                                FileOutputStream(pdfFile).use { fos ->
                                    document!!.writeTo(fos)
                                }
                            }

                            progressDialog.dismiss()
                            Toast.makeText(context, "PDF created successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            progressDialog.dismiss()
                            Toast.makeText(context, "PDF creation failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            document?.close()
                        }
                    }

                    progressDialog.show()
                }

            }
        }
    }

    override fun getItemCount(): Int = images.size

    private fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            BitmapFactory.decodeResource(context.resources, R.drawable.dog)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<ImageEntity>) {
        images = newData
        notifyDataSetChanged()
    }
}
