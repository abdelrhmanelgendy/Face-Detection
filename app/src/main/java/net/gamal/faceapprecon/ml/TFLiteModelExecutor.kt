package net.gamal.faceapprecon.ml

import android.graphics.Bitmap
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import net.gamal.faceapprecon.facedetection.presentation.FaceDetectionActivity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.sqrt

object TFLiteModelExecutor {
    fun executeTensorModel(
        lifecycleScope: LifecycleCoroutineScope,
        faceDetectionActivity: FaceDetectionActivity,
        faceBitmap: Bitmap?,
        onSuccess: (FloatArray) -> Unit
    ) {
        val faceNetImageProcessor = ImageProcessor.Builder().add(
            ResizeOp(
                112, 112, ResizeOp.ResizeMethod.BILINEAR
            )
        ).add(NormalizeOp(0f, 255f)).build()
        val tensorImage = TensorImage.fromBitmap(faceBitmap)

        val faceNetByteBuffer = faceNetImageProcessor.process(tensorImage).buffer

        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 112, 112, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(faceNetByteBuffer)


        lifecycleScope.launch {
            val model = MobileFaceNet.newInstance(faceDetectionActivity)
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val faceEncodedData = outputFeature0.floatArray
            onSuccess(faceEncodedData)
        }
    }

   private fun findNearestFace(embedding1: FloatArray, embeddingList: List<FloatArray>): Pair<Int, Float>? {
        // Normalize current face embedding
        val norm1 = sqrt(embedding1.sumByDouble { it.toDouble() * it }.toFloat())
        val normalizedEmbedding1 = embedding1.map { it / norm1 }.toFloatArray()

        var nearestIndex: Int? = null
        var minDistance = Float.MAX_VALUE

        // Iterate through the list of face embeddings
        for ((index, embedding2) in embeddingList.withIndex()) {
            // Normalize face embedding from the list
            val norm2 = sqrt(embedding2.sumByDouble { it.toDouble() * it }.toFloat())
            val normalizedEmbedding2 = embedding2.map { it / norm2 }.toFloatArray()

            // Compute Euclidean distance between current face and face from the list
            var sum = 0.0f
            for (i in normalizedEmbedding1.indices) {
                val diff = normalizedEmbedding1[i] - normalizedEmbedding2[i]
                sum += diff * diff
            }
            val distance = sqrt(sum)

            // Update nearest face index and distance if closer than previous closest face
            if (distance < minDistance) {
                nearestIndex = index
                minDistance = distance
            }
        }

        // Check if the minimum distance is within the threshold
        if (minDistance <= 0.9) {
            return Pair(nearestIndex!!, minDistance)
        } else {
            return null // No face found within the threshold
        }
    }
}