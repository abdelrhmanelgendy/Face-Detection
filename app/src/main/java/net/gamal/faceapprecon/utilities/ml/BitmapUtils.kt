package net.gamal.faceapprecon.utilities.ml

import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer

internal object BitmapUtils {

    // Create a bitmap with the specified width and height
    internal fun createBitmap(width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    // Resize the bitmap to the specified width and height
    private fun Bitmap.resizeBitmap(newWidth: Int, newHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
    }

    // Convert the bitmap to a byte buffer
    fun Bitmap.convertBitmapToByteBuffer(inputBuffer: ByteBuffer) {
        inputBuffer.rewind()
        val pixels = IntArray(width * height)
        getPixels(pixels, 0, width, 0, 0, width, height)

        for (pixelValue in pixels) {
            if (inputBuffer.remaining() < 3 * java.lang.Float.BYTES) {
                // Ensure there's enough space in the ByteBuffer
                break
            }
            inputBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
            inputBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }
    }

    // Run inference on the bitmap using the provided interpreter and input buffer
    fun runInference(bitmap: Bitmap, interpreter: Interpreter, inputBuffer: ByteBuffer) {
        try {
            val inputShape = interpreter.getInputTensor(0).shape()
            val inputSize = inputShape[1]
            val expectedBufferSize = interpreter.getInputTensor(0).numBytes()
            val resizedBitmap = bitmap.resizeBitmap(inputSize, inputSize)

            if (resizedBitmap.byteCount > expectedBufferSize) {
                Log.e(TAG, "Resized bitmap byte count exceeds expected buffer size")
                return
            }

            resizedBitmap.convertBitmapToByteBuffer(inputBuffer)
        } catch (e: Exception) {
            Log.e(TAG, "${e.message}")
            return
        }

    }

    private const val TAG = "BitmapUtils"
}
