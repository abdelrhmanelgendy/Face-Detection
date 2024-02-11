package net.gamal.faceapprecon.utilities.ml


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import net.gamal.faceapprecon.ml.MobileFaceNet
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EncodedFaceModelExecutor(context: Context) {

    private val model = MobileFaceNet.newInstance(context)

    fun executeTensorModel(
        lifecycleScope: LifecycleCoroutineScope,
        faceBitmap: Bitmap?,
        onSuccess: (FloatArray) -> Unit
    ) {
        val inputImageWidth = 224
        val inputImageHeight = 224

        val resizedBitmap =
            Bitmap.createScaledBitmap(faceBitmap!!, inputImageWidth, inputImageHeight, true)

        val normalizedBitmap = resizedBitmap.normalizePixelValues()

        val inputByteBuffer =
            ByteBuffer.allocateDirect(inputImageWidth * inputImageHeight * 3 * 4) // 4 bytes per float
        inputByteBuffer.order(ByteOrder.nativeOrder())
        normalizedBitmap.copyPixelsToBuffer(inputByteBuffer)
        inputByteBuffer.rewind()

        val inputFeature0 = TensorBuffer.createFixedSize(
            intArrayOf(1, inputImageWidth, inputImageHeight, 3),
            DataType.FLOAT32
        )
        inputFeature0.loadBuffer(inputByteBuffer)

        lifecycleScope.launch {
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val faceEncodedData = outputFeature0.floatArray
            println("Face encoded data: ${faceEncodedData.contentToString()}")
            onSuccess(faceEncodedData)
        }
    }

    private fun Bitmap.normalizePixelValues(): Bitmap {
        val normalizedBitmap = this.copy(this.config, true)
        val pixels = IntArray(width * height)
        normalizedBitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = (Color.red(pixel) - 127.5f) / 127.5f
            val green = (Color.green(pixel) - 127.5f) / 127.5f
            val blue = (Color.blue(pixel) - 127.5f) / 127.5f
            pixels[i] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
        }
        normalizedBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return normalizedBitmap
    }
}