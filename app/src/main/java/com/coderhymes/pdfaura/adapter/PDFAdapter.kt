package com.coderhymes.pdfaura.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coderhymes.pdfaura.R
import com.coderhymes.pdfaura.fragment.PdfModel
import com.coderhymes.pdfaura.screens.PdfViewActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PDFAdapter(private val context: Context, private val pdfList: MutableList<PdfModel>) :
    RecyclerView.Adapter<PDFAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.item_pdf, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pdfList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val pdfModel = pdfList[position]
        holder.filename.text = pdfModel.fileName
        holder.filesize.text = formatFileSize(pdfModel.fileSize.toLong())
        holder.filedate.text = formatDate(pdfModel.fileDate)
        val filePath = pdfModel.filePath

        // Now you can use the filePath as needed
        holder.itemView.setOnClickListener {
            // Example: Open PDF viewer activity with the selected file path
            openPdfViewerActivity(filePath)
        }
    }

    private fun openPdfViewerActivity(filePath: String) {
        val intent = Intent(context, PdfViewActivity::class.java)
        intent.putExtra("pdfFilePath", filePath)
        context.startActivity(intent)
    }

    private fun formatDate(date: Date?): String {
        date?.let {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return simpleDateFormat.format(it)
        }
        return ""
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        val fileSizeKB = sizeInBytes / 1024.0
        val fileSizeMB = fileSizeKB / 1024.0

        return when {
            fileSizeMB >= 1.0 -> String.format(Locale.getDefault(), "%.2f MB", fileSizeMB)
            fileSizeKB >= 1.0 -> String.format(Locale.getDefault(), "%.2f KB", fileSizeKB)
            else -> String.format(Locale.getDefault(), "%d B", sizeInBytes)
        }
    }

    fun updateList(newList: List<PdfModel>) {
        pdfList.clear()
        pdfList.addAll(newList)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var filename: TextView = itemView.findViewById(R.id.file_name)
        var filesize: TextView = itemView.findViewById(R.id.file_size)
        var filedate: TextView = itemView.findViewById(R.id.file_date)
    }
}
