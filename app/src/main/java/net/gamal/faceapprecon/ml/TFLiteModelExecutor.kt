package net.gamal.faceapprecon.ml

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch
import net.gamal.faceapprecon.MainActivity
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
        faceBitmap: Bitmap?
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
            Log.d(MainActivity.TAG, "Data : faceEncodedData : ${faceEncodedData.contentToString()}")

//            val computeDistance = computeDistance(faceEncodedData, mostafa1)
//            if (computeDistance<.9)
//            {
//                Log.d(MainActivity.TAG, "executeTensorModel: The Face Is Mostafa")
//            }else
//            {
//                Log.d(MainActivity.TAG, "executeTensorModel: The Face Is X")
//
//            }
        }
    }
    
    val mostafa1:FloatArray = floatArrayOf(
        -0.009653989f,
        0.013591457f,
        8.3232E-4f,
        0.0066747903f,
        -0.023485586f,
        0.10871566f,
        -0.04624291f,
        0.062366307f,
        0.036170784f,
        0.19630192f,
        9.822595E-4f,
        0.009313903f,
        4.0380964E-5f,
        -0.01038772f,
        -0.003037334f,
        0.0018793553f,
        -0.0154181365f,
        -0.0049693333f,
        0.0048044357f,
        0.010952248f,
        0.1253516f,
        0.020119146f,
        0.14209397f,
        0.009442959f,
        0.096619435f,
        0.012096391f,
        -0.016041474f,
        0.067431785f,
        0.06991369f,
        5.9918565E-4f,
        -9.171027E-4f,
        0.22350638f,
        -0.03809722f,
        -0.004870264f,
        -0.097066276f,
        -0.22077483f,
        0.28866187f,
        0.014170941f,
        -0.001612955f,
        -0.024882782f,
        -0.0032549954f,
        0.0017060024f,
        0.013111096f,
        -1.482534E-4f,
        0.002070836f,
        -0.024615336f,
        -0.11394314f,
        -0.120232575f,
        7.588874E-4f,
        0.051918805f,
        0.090748556f,
        -0.0073454436f,
        -0.15303521f,
        -0.0012722694f,
        -0.17031336f,
        0.014308054f,
        -0.010907255f,
        0.0020003063f,
        -0.031372957f,
        0.005451049f,
        0.09005595f,
        -0.020285493f,
        -0.10711076f,
        0.25618973f,
        -0.013203187f,
        0.011536242f,
        -0.00664472f,
        -0.016089961f,
        0.016240101f,
        0.002301904f,
        -0.015332909f,
        -0.08203267f,
        -0.22198361f,
        -0.010882164f,
        -0.1208477f,
        -0.0032409537f,
        0.0011190139f,
        0.0011362832f,
        0.007950586f,
        0.031380765f,
        -0.008565701f,
        -0.052790705f,
        0.0025772522f,
        0.0069005312f,
        -0.024175238f,
        -0.0039227647f,
        0.0027236138f,
        0.08143605f,
        0.055882145f,
        0.17522968f,
        -0.14498818f,
        -0.0046246466f,
        -8.541081E-4f,
        -0.013353698f,
        0.027301544f,
        -0.007487721f,
        0.043613747f,
        -0.052041497f,
        -0.0037521967f,
        -0.0010346215f,
        0.0041662883f,
        -0.0016552407f,
        0.0048123114f,
        -0.0012379056f,
        0.0066111702f,
        0.007044543f,
        -0.056853402f,
        0.0020314325f,
        1.033478E-4f,
        0.011494158f,
        -0.01621523f,
        0.0023304222f,
        0.008260688f,
        0.33188286f,
        0.013828674f,
        -0.0753344f,
        -0.0057201874f,
        0.02351174f,
        -0.025764713f,
        -0.0050239526f,
        0.20195916f,
        -0.01671688f,
        -0.17504895f,
        1.5391625E-4f,
        0.0027067077f,
        -0.003271003f,
        0.0042851577f,
        0.010095463f,
        -0.009025519f,
        -0.034114365f,
        0.0020113736f,
        0.019475672f,
        -0.0034089168f,
        0.0073598484f,
        0.020400058f,
        -0.007905485f,
        -0.09460322f,
        -0.035739977f,
        0.008000198f,
        -0.005325313f,
        0.0047348463f,
        -0.005936208f,
        -0.0038699752f,
        -0.17524639f,
        0.02043278f,
        0.059540015f,
        -0.010839752f,
        -0.004639489f,
        0.011435843f,
        -2.9057986E-4f,
        -0.008902777f,
        -0.15607536f,
        -0.2047963f,
        -0.009034101f,
        -0.007832922f,
        -0.011107849f,
        0.005617233f,
        -0.004790507f,
        0.109132946f,
        -0.0016900388f,
        -0.023899233f,
        0.0070904014f,
        -0.0026926044f,
        5.641128E-5f,
        0.0018369363f,
        0.01386964f,
        -0.004273633f,
        0.06064547f,
        -4.0064045E-4f,
        -2.2288418E-4f,
        0.11271509f,
        -0.029476382f,
        -0.0026266163f,
        -0.0066614556f,
        0.0181405f,
        0.0034371922f,
        0.015744947f,
        -0.028791288f,
        -0.008212781f,
        0.0028950232f,
        0.0774306f,
        -0.01739675f,
        -5.689714E-4f,
        0.0016251449f,
        -0.10079783f,
        -0.031979647f,
        -0.015608784f,
        0.029424503f,
        -0.04966164f,
        -0.094798096f,
        -0.021665024f,
        0.0099325385f
    )

    fun computeDistance(embedding1: FloatArray, embedding2: FloatArray): Float {
        // Normalize embeddings
        val norm1 = sqrt(embedding1.sumByDouble { it.toDouble() * it }.toFloat())
        val norm2 = sqrt(embedding2.sumByDouble { it.toDouble() * it }.toFloat())
        val normalizedEmbedding1 = embedding1.map { it / norm1 }.toFloatArray()
        val normalizedEmbedding2 = embedding2.map { it / norm2 }.toFloatArray()

        // Compute Euclidean distance
        var sum = 0.0f
        for (i in normalizedEmbedding1.indices) {
            val diff = normalizedEmbedding1[i] - normalizedEmbedding2[i]
            sum += diff * diff
        }
        return sqrt(sum)
    }

    private val registered: HashMap<String, FloatArray> = hashMapOf(
        "MOSTAFA" to mostafa1
    )
}