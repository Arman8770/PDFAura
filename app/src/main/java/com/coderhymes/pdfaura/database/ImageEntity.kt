package com.coderhymes.pdfaura.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) var Iid: Int = 0, // Change the default value to 0
    @ColumnInfo(name = "imagesUri") val uri: String?
)


