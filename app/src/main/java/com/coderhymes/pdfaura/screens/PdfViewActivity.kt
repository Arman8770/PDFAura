package com.coderhymes.pdfaura.screens

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.coderhymes.pdfaura.R
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class PdfViewActivity : AppCompatActivity() {
    private lateinit var pdfView: PDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)

        pdfView = findViewById(R.id.pdfView)

        // Get the PDF file URI or file path from the intent
        val pdfUri = intent.data
        val pdfFilePath = intent.getStringExtra("pdfFilePath")

        // Load the PDF file
        if (pdfUri != null) {
            displayPDFUri(pdfUri)
        } else if (pdfFilePath != null) {
            displayPDF(File(pdfFilePath))
        } else {
            Log.e("PdfViewActivity", "No PDF URI or file path provided")
            // Handle the case where neither URI nor file path is provided
            // You may want to show an error message or handle it accordingly
        }
    }

    private fun displayPDF(pdfFile: File) {
        try {
            pdfView.fromFile(pdfFile)
                .scrollHandle(DefaultScrollHandle(this))
                .spacing(10) // in dp
                .load()
        } catch (e: Exception) {
            handlePdfLoadError(e, "from file")
        }
    }

    private fun displayPDFUri(pdfUri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(pdfUri)

            if (inputStream != null) {
                try {
                    val tempFile = File.createTempFile("temp_pdf", ".pdf", cacheDir)
                    copyInputStreamToFile(inputStream, tempFile)

                    pdfView.fromFile(tempFile)
                        .scrollHandle(DefaultScrollHandle(this))
                        .spacing(10) // in dp
                        .load()
                } catch (e: Exception) {
                    handlePdfLoadError(e, "from URI")
                } finally {
                    inputStream.closeQuietly()
                }
            } else {
                Log.e("PdfViewActivity", "Input stream is null")
                // Handle the case where InputStream is null
                // You may want to show an error message or handle it accordingly
            }
        } catch (e: IOException) {
            handlePdfLoadError(e, "opening input stream")
        }
    }

    private fun copyInputStreamToFile(inputStream: InputStream, file: File) {
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
        }
    }

    private fun handlePdfLoadError(e: Exception, source: String) {
        e.printStackTrace()
        Log.e("PdfViewActivity", "Error loading PDF $source: ${e.message}")
        // Handle exceptions related to PDF loading
        // You may want to show an error message or handle it accordingly
    }

    private fun InputStream.closeQuietly() {
        try {
            close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}