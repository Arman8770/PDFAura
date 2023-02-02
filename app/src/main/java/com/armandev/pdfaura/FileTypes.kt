package com.armandev.pdfaura

import android.webkit.MimeTypeMap

enum class FileTypes(val mimeTypes: List<String?>) {
        PDF(
            listOf(
                MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf"),
            )
        )
}