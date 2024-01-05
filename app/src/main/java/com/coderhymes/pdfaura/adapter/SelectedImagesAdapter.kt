package com.coderhymes.pdfaura.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.coderhymes.pdfaura.R
import com.coderhymes.pdfaura.database.ImageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectedImagesAdapter(
    private var images: List<ImageEntity>,
//    private val filterListener: FilterSelectionListener
) : RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_view, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Log.d("ImageAdapter", "onBindViewHolder called for position: $position")

        // Ensure position is within bounds
        if (position < 0 || position >= images.size) {
            Log.e("ImageAdapter", "Invalid position: $position")
            return
        }

        val imageUri = images[position].uri
        Log.d("ImageAdapter", "Image URI at position $position: $imageUri")

        // Load image from URI using a background thread
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = loadBitmapFromUri(Uri.parse(imageUri), holder.imageView.context)

            // Switch to the main thread to update the UI
            withContext(Dispatchers.Main) {
                // Ensure the view holder still corresponds to the correct position
                if (holder.adapterPosition == position) {
                    holder.imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    override fun getItemCount(): Int = images.size

    private fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap {
        Log.d("ImageAdapter2", "loadBitmapFromUri called for URI: $uri")
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

    fun updateData(newData: List<ImageEntity>) {
        images = newData
        notifyDataSetChanged()
    }
}
