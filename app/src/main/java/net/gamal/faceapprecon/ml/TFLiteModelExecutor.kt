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

}