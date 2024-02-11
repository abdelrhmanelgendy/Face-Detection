package net.gamal.faceapprecon.detection.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class LocalBitmapRepository(private val context: Context) {
    private val IMAGES_DIR = "face_detection_images"

    fun saveBitmapToCacheDirectory(bitmap: Bitmap, fileName: String) {
        val directory = File(context.cacheDir, IMAGES_DIR) // Get the cache directory
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "$fileName.png") // Create a new file in the cache directory

        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(
                    Bitmap.CompressFormat.PNG, 100, fos
                ) // Compress bitmap to PNG format and write it to the file
                fos.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getSavedFileFromInternalCache(fileName: String): Bitmap? {
        val directory = File(context.cacheDir, IMAGES_DIR)
        if (!directory.exists()) {
            // Directory does not exist, file hasn't been saved
            return null
        }

        val file = File(directory, "$fileName.png")
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }
}
