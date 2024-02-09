package net.gamal.faceapprecon.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import com.google.mlkit.vision.face.Face

object ImageDetectorUtil {
      fun biggestFace(faces: List<Face>): Face? {
        var biggestFace: Face? = null
        var biggestFaceSize = 0
        for (face in faces) {
            val faceSize = face.boundingBox.height() * face.boundingBox.width()
            if (faceSize > biggestFaceSize) {
                biggestFaceSize = faceSize
                biggestFace = face
            }
        }
        return biggestFace
    }

    fun cropFaceToBBox(image: Bitmap, boundingBox: Rect, rotation: Int): Bitmap? {
        var image = image
        val shift = 0
        if (rotation != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            image = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        }
        return if (boundingBox.top >= 0 && boundingBox.bottom <= image.width && boundingBox.top + boundingBox.height() <= image.height && boundingBox.left >= 0 && boundingBox.left + boundingBox.width() <= image.width) {
            Bitmap.createBitmap(
                image,
                boundingBox.left,
                boundingBox.top + shift,
                boundingBox.width(),
                boundingBox.height()
            )
        } else null
    }
}