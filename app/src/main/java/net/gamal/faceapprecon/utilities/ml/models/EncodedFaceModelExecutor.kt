package net.gamal.faceapprecon.utilities.ml.models


import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LifecycleCoroutineScope
import net.gamal.faceapprecon.ml.Mobilenetv2Embedding
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EncodedFaceModelExecutor(context: Context) {

    private val model = Mobilenetv2Embedding.newInstance(context)

    fun executeTensorModel(
        lifecycleScope: LifecycleCoroutineScope,
        faceBitmap: Bitmap?,
        onSuccess: (FloatArray) -> Unit
    ) {
        if (faceBitmap == null) return
        val tensorImage = TensorImage(DataType.FLOAT32)

        tensorImage.load(faceBitmap)

        val resized = Bitmap.createScaledBitmap(faceBitmap, 224, 224, true)

        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3).apply {
            order(ByteOrder.nativeOrder())
            val intValues = IntArray(224 * 224)
            resized.getPixels(intValues, 0, resized.width, 0, 0, resized.width, resized.height)
            for (pixelValue in intValues) {
                putFloat(((pixelValue shr 16 and 0xFF) - 127.5f) / 127.5f)
                putFloat(((pixelValue shr 8 and 0xFF) - 127.5f) / 127.5f)
                putFloat(((pixelValue and 0xFF) - 127.5f) / 127.5f)
            }
            rewind()
        }

        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
        println("executeTensorModel outputFeature0:: ${outputFeature0.floatArray.contentToString()}")
        onSuccess(outputFeature0.floatArray)
    }
}