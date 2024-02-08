package net.gamal.faceapprecon.facedetextion.data.model


import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.google.mlkit.vision.face.Face
import net.gamal.faceapprecon.facedetextion.presentation.FaceBoxOverlay

class FaceBox(
    overlay: FaceBoxOverlay,
    private val face: Face,
    private val imageRect: Rect,
    val originalBitmap: Bitmap
) : FaceBoxOverlay.FaceBox(overlay) {

       var bitMap: Bitmap =originalBitmap
    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 15.0f
    }

    override fun draw(canvas: Canvas?) {
        val rect = getBoxRect(
            imageRectWidth = imageRect.width().toFloat(),
            imageRectHeight = imageRect.height().toFloat(),
            faceBoundingBox = face.boundingBox
        )
        canvas?.drawRect(rect, paint)
     this.bitMap = rescaleAndCropBitmap(originalBitmap, rect, 100, 100)
    }

    fun rescaleAndCropBitmap(originalBitmap: Bitmap, cropRect: RectF, newWidth: Int, newHeight: Int): Bitmap {
        // Create a Matrix to scale the bitmap
        val matrix = Matrix()

        // Calculate the scale factors
        val scaleX = newWidth.toFloat() / cropRect.width()
        val scaleY = newHeight.toFloat() / cropRect.height()

        // Apply the scale factors
        matrix.postScale(scaleX, scaleY)

        // Create a Bitmap with the desired dimensions for the cropped image
        val croppedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        // Create a Canvas to draw the cropped bitmap onto the new Bitmap
        val canvas = Canvas(croppedBitmap)

        // Translate the canvas to match the crop region
        canvas.translate(-cropRect.left * scaleX, -cropRect.top * scaleY)

        // Draw the original bitmap onto the canvas using the matrix
        canvas.drawBitmap(originalBitmap, matrix, null)

        // Optionally recycle the original bitmap to free up memory
//        originalBitmap.recycle()

        return croppedBitmap
    }

}