package com.armandev.pdfaura

import android.net.Uri

data class PDFdata(
    val id: Long, val title:String, val size: Long,
    val path:String, val pdfUri: Uri, val date: Long, val type:String)
