package net.gamal.faceapprecon.ml

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.gamal.faceapprecon.MainActivity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

object TFLiteModelExecutor {
    fun executeTensorModel(
        lifecycleScope: CoroutineScope,
        context: Context,
        inputBuffer: ByteBuffer,
    ) {
        lifecycleScope.launch {
            val model = FaceRecognitionMobilenetv2.newInstance(context)
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