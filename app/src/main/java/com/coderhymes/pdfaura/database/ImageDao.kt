package com.coderhymes.pdfaura.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ImageDao {
    @Query("SELECT * FROM ImageEntity")
    fun getAll(): List<ImageEntity>

    @Query("SELECT * FROM ImageEntity WHERE Iid IN (:imageIds)")
    fun loadAllByIds(imageIds: IntArray): List<ImageEntity>

    @Query("SELECT * FROM ImageEntity WHERE imagesUri LIKE :imageUri")
    fun findByName(imageUri: String): ImageEntity?

    @Insert
    fun insert(image: ImageEntity)

    @Update
    fun update(image: ImageEntity)

    @Delete
    fun delete(image: ImageEntity)
}

