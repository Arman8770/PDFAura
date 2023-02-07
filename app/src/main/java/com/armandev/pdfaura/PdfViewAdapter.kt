package com.armandev.pdfaura

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class PdfViewAdapter : RecyclerView.Adapter<PdfViewAdapter.PdfViewHolder>(), Filterable {
    var tempList: ArrayList<PDFdata> = ArrayList()
    var backup :ArrayList<PDFdata> = ArrayList()

    var onItemClick: ((PDFdata) -> Unit)? = null

    inner class PdfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pdfName: TextView = itemView.findViewById(R.id.pdf_nameView)
        val pdfSize: TextView = itemView.findViewById(R.id.txtpdfSize)
        val pdfDate:TextView = itemView.findViewById(R.id.txtpdfDate)
        val pdfTouch: RelativeLayout = itemView.findViewById(R.id.pdfTouch)
        init {
            itemView.setOnClickListener{
                onItemClick?.invoke(backup[adapterPosition])
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        return PdfViewHolder(
            LayoutInflater.from(parent.context).
        inflate(R.layout.single_row_design,parent,false))
    }

    override fun getItemCount(): Int {
        return tempList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addData(list: List<PDFdata>) {
        tempList = list as ArrayList<PDFdata>
        backup = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        holder.pdfName.text = tempList[position].title

        val date =  convertLongToTime(tempList[position].date)

        holder.pdfDate.text = date
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
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            holder.pdfTouch.context.startActivity(intent)
        }

    }
    private fun convertLongToTime(time: Long): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(time * 1000)
    }

    override fun getFilter(): Filter {
        return filter
    }

    private var filter: Filter = object : Filter() {
        override fun performFiltering(keyWord: CharSequence?): FilterResults {
            val filteredList = ArrayList<PDFdata>()

            if (keyWord.toString().isEmpty()) {
                filteredList.addAll(backup)
            } else{
                for(obj in backup){
                    if(obj.title.lowercase().contains(keyWord.toString().lowercase())){
                        filteredList.add(obj)
                    }
                }
            }
            var results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            tempList.clear()
            tempList.addAll(results?.values as ArrayList<PDFdata>)
            notifyDataSetChanged()
        }
    }
}