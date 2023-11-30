package com.coderhymes.pdfaura.fragment

import java.util.Date

data class PdfModel(
    val fileName: String,
    val fileSize: String,
    val pageCount: Int,
    val fileType: String,
    val filePath: String,
    val fileDate: Date
)
