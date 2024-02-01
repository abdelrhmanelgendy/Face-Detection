package net.gamal.faceapprecon.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.gamal.faceapprecon.MainActivity
import net.gamal.faceapprecon.ml.ImageUtils.toTBitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

object TFLiteModelExecutor {
    fun executeTensorModel(
        lifecycleScope: CoroutineScope,
        context: Context,
        inputData: FloatArray,
    ) {
        lifecycleScope.launch {
            // Ensure inputData is not empty
            if (inputData.isEmpty()) {
                // Handle the case when inputData is empty
                return@launch
            }

            val inputSize = inputData.size
            val inputBuffer =
                ByteBuffer.allocateDirect(inputSize * java.lang.Float.SIZE / java.lang.Byte.SIZE)
            inputBuffer.order(ByteOrder.nativeOrder())

            // Copy the float data into the ByteBuffer
            for (value in inputData) {
                inputBuffer.putFloat(value)
            }

            // Reset the position to the beginning of the buffer before passing it to the model
            inputBuffer.rewind()

            val model = ModelUnquant.newInstance(context)
            Log.e(MainActivity.TAG, "executeTensorModel: model=$model ")

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            Log.e(MainActivity.TAG, "executeTensorModel: inputFeature0=$inputFeature0 ")
            inputFeature0.loadBuffer(inputBuffer)

            // Runs model inference and gets the result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            Log.e(MainActivity.TAG, "executeTensorModel: outputs=${outputs} ")
            Log.e(MainActivity.TAG, "executeTensorModel: outputFeature0=${outputFeature0.buffer} ")


            // Interpret the model output (for example, assuming it's classification probabilities)
            val probabilities = outputFeature0.floatArray
            Log.e(MainActivity.TAG, "executeTensorModel: probabilities=${probabilities} ")

            // Find the index of the maximum probability (assuming it's a classification task)
            var maxProbabilityIndex = 0
            var maxProbability = probabilities[0]

            for (i in 1 until probabilities.size) {
                if (probabilities[i] > maxProbability) {
                    maxProbability = probabilities[i]
                    maxProbabilityIndex = i
                }
            }

            // Print the result
            println("Predicted class index: $maxProbabilityIndex")
            println("Probability: ${probabilities[maxProbabilityIndex]}")


            // Releases model resources if no longer used.
            model.close()
        }
    }

    fun executeTensorModel(
        lifecycleScope: CoroutineScope,
        context: Context,
        inputBuffer: ByteBuffer,
    ) {
        lifecycleScope.launch {
            val model = ModelUnquant.newInstance(context)
            Log.e(MainActivity.TAG, "executeTensorModel: model=$model ")

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            Log.e(MainActivity.TAG, "executeTensorModel: inputFeature0=$inputFeature0 ")
            inputFeature0.loadBuffer(inputBuffer)

            // Runs model inference and gets the result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            Log.e(MainActivity.TAG, "executeTensorModel: outputs=${outputs} ")
            Log.e(MainActivity.TAG, "executeTensorModel: outputFeature0=${outputFeature0.buffer} ")


            // Interpret the model output (for example, assuming it's classification probabilities)
            val probabilities = outputFeature0.floatArray
            Log.e(MainActivity.TAG, "executeTensorModel: probabilities=${probabilities} ")

            // Find the index of the maximum probability (assuming it's a classification task)
            var maxProbabilityIndex = 0
            var maxProbability = probabilities[0]

            for (i in 1 until probabilities.size) {
                if (probabilities[i] > maxProbability) {
                    maxProbability = probabilities[i]
                    maxProbabilityIndex = i
                }
            }

            // Print the result
            println("Predicted class index: $maxProbabilityIndex")
            println("Probability: ${probabilities[maxProbabilityIndex]}")
            Log.e(
                MainActivity.TAG,
                "executeTensorModel: Predicted class index: maxProbabilityIndex=$maxProbabilityIndex "
            )
            Log.e(
                MainActivity.TAG,
                "executeTensorModel: Probability=${probabilities[maxProbabilityIndex]} "
            )


            // Releases model resources if no longer used.
//            model.close()
        }
    }

    fun executeTensorModel(
        lifecycleScope: CoroutineScope,
        context: Context,
        imageProxy: ImageProxy
    ) {
        val tBitmap=imageProxy.toTBitmap()

        // Assuming tBitmap is the original Bitmap
        val originalWidth = tBitmap?.width ?: 0
        val originalHeight = tBitmap?.height ?: 0

// Specify the region you want to extract
        val targetWidth = 224
        val targetHeight = 244

// Calculate the starting point for the extraction
        val startX = (originalWidth - targetWidth) / 2
        val startY = (originalHeight - targetHeight) / 2

// Create a new Bitmap containing only the desired region
        val croppedBitmap = Bitmap.createBitmap(tBitmap!!, startX, startY, targetWidth, targetHeight)

        val inputBuffer: ByteBuffer = ByteBuffer.allocate(224 * 244  * 3)

// Ensure proper buffer state before copying
        inputBuffer.rewind()

        croppedBitmap.copyPixelsToBuffer(inputBuffer)
        inputBuffer.rewind()


        lifecycleScope.launch {
            val model = ModelUnquant.newInstance(context)
            Log.e(MainActivity.TAG, "executeTensorModel: model=$model ")

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            Log.e(MainActivity.TAG, "executeTensorModel: inputFeature0=$inputFeature0 ")
            inputFeature0.loadBuffer(inputBuffer)

            // Runs model inference and gets the result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            Log.e(MainActivity.TAG, "executeTensorModel: outputs=${outputs} ")
            Log.e(MainActivity.TAG, "executeTensorModel: outputFeature0=${outputFeature0.buffer} ")


            // Interpret the model output (for example, assuming it's classification probabilities)
            val probabilities = outputFeature0.floatArray
            Log.e(MainActivity.TAG, "executeTensorModel: probabilities=${probabilities} ")

            // Find the index of the maximum probability (assuming it's a classification task)
            var maxProbabilityIndex = 0
            var maxProbability = probabilities[0]

            for (i in 1 until probabilities.size) {
                if (probabilities[i] > maxProbability) {
                    maxProbability = probabilities[i]
                    maxProbabilityIndex = i
                }
            }

            // Print the result
            println("Predicted class index: $maxProbabilityIndex")
            println("Probability: ${probabilities[maxProbabilityIndex]}")
            Log.e(
                MainActivity.TAG,
                "executeTensorModel: Predicted class index: maxProbabilityIndex=$maxProbabilityIndex "
            )
            Log.e(
                MainActivity.TAG,
                "executeTensorModel: Probability=${probabilities[maxProbabilityIndex]} "
            )


            // Releases model resources if no longer used.
//            model.close()
        }
    }

}