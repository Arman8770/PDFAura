package com.coderhymes.pdfaura.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.coderhymes.pdfaura.R
import com.coderhymes.pdfaura.database.ImageEntity

class SelectedImagesAdapter(
    private var imageUris: List<Uri>,
    private val filter: (Bitmap) -> Unit
) : RecyclerView.Adapter<SelectedImagesAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_view, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = imageUris[position]

        // Load image from URI using an appropriate method (e.g., Glide or Picasso)
        val bitmap = loadBitmapFromUri(uri, holder.imageView.context)

        // Apply the chosen filter to the image
        filter(bitmap)

        // Set the filtered image to the ImageView
        holder.imageView.setImageBitmap(bitmap)
    }

    override fun getItemCount(): Int = imageUris.size

    // Add a method to load a Bitmap from a URI using your preferred method/library
    private fun loadBitmapFromUri(uri: Uri, context: Context): Bitmap {
        return try {
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the exception (e.g., log or throw a custom exception)
            // Return a placeholder bitmap or null
            BitmapFactory.decodeResource(context.resources, R.drawable.dog)
        }
    }

    fun updateData(newData: List<ImageEntity>) {
//        imageUris = newData
        notifyDataSetChanged()
    }
}
