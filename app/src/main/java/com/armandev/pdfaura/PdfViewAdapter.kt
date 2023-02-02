package com.armandev.pdfaura

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PdfViewAdapter(private val tempList: ArrayList<PDFdata>) :
    RecyclerView.Adapter<PdfViewAdapter.PdfViewHolder>() {
    class PdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfName: TextView = itemView.findViewById(R.id.pdf_nameView)
        val pdfSize: TextView = itemView.findViewById(R.id.txtpdfSize)
        val pdafDate:TextView = itemView.findViewById(R.id.txtpdfDate)
        val pdfTouch: RelativeLayout = itemView.findViewById(R.id.pdfTouch)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        return PdfViewHolder(LayoutInflater.from(parent.context).
        inflate(R.layout.single_row_design,parent,false))
    }

    override fun getItemCount(): Int {
        return tempList.size
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        holder.pdfName.text = tempList[position].title
        val sizeinKb = tempList[position].size/1024
        val sizeinMb = sizeinKb/1024
        val sizeinGb = sizeinKb/1024
        if (sizeinKb>=0 && sizeinKb< 1024){
            holder.pdfSize.text = sizeinKb.toString() + "KB"
        }
        else if (sizeinMb>=0 && sizeinMb<1024){
            holder.pdfSize.text = sizeinMb.toString() + "MB"
        }
        else if (sizeinGb>=0 && sizeinGb<1024){
            holder.pdfSize.text = sizeinGb.toString() + "GB"
        }


        holder.pdfTouch.setOnClickListener{
            val intent = Intent(holder.pdfTouch.context,PdfViewActivity::class.java)
            intent.putExtra("filename",tempList[position].title)
            intent.putExtra("fileurl",tempList[position].pdfUri)
            intent.putExtra("filepath",tempList[position].path)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            holder.pdfTouch.context.startActivity(intent)

        }
    }
}