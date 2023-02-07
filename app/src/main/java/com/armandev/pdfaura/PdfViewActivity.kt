package com.armandev.pdfaura

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import java.io.File


class PdfViewActivity : AppCompatActivity() {

    private lateinit var pdfView:PDFView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)

        val pdfName = intent.getStringExtra("filename").toString()
        val pdfUri = intent.getStringExtra("fileurl").toString()
        val pdfPath = intent.getStringExtra("filepath").toString()

        val file = File(pdfPath)
        val path:Uri = Uri.fromFile(file)

        pdfView = findViewById(R.id.pdfView)
        pdfView.fromUri(path)
            .load()



    }
}