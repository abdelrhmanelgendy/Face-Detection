package net.gamal.faceapprecon.camera.data.model


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.google.mlkit.vision.face.Face
import net.gamal.faceapprecon.presentation.views.FaceBoxOverlay

class FaceBox(
    private val overlay: FaceBoxOverlay,
    private val face: Face,
    private val imageRect: Rect,
    private val requireFlip: Boolean
) : FaceBoxOverlay.FaceBox(overlay) {

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
       if(requireFlip){
           flipTheRect(rect)
       }
        canvas?.drawRect(rect, paint)
    }

    private fun flipTheRect(rect: RectF) {
        val screenWidth = overlay.width
        val newLeft = screenWidth - rect.right
        val newRight = screenWidth - rect.left
        rect.left = newLeft
        rect.right = newRight
    }

}