package net.gamal.faceapprecon.facedetextion.presentation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.face.Face
import net.gamal.faceapprecon.databinding.ActivityFaceDetectionBinding
import net.gamal.faceapprecon.facedetextion.data.FaceDetectionRepository
import net.gamal.faceapprecon.facedetextion.data.model.FaceBox
import net.gamal.faceapprecon.ml.TFLiteModelExecutor
import net.gamal.faceapprecon.ml.TFLiteModelLoader
import net.gamal.faceapprecon.utils.MediaUtils.flip
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors


class FaceDetectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceDetectionBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private val cameraXViewModel = viewModels<CameraXViewModel>()
    private val faceDetectionRepository by lazy {
        FaceDetectionRepository()
    }
    private lateinit var inputBuffer: ByteBuffer
    private lateinit var interpreter: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        interpreter = Interpreter(
            TFLiteModelLoader.loadModelFile(
                assets,
                "face_recognition_mobilenetv2.tflite"
            )
        )
        val inputSize = interpreter.getInputTensor(0).shape()[1]
        inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        cameraXViewModel.value.processCameraProvider.observe(this) { provider ->
            processCameraProvider = provider
            bindCameraPreview()
            bindInputAnalyser()
        }
    }

    private fun bindCameraPreview() {
        cameraPreview =
            Preview.Builder().setTargetRotation(binding.cameraPreview.display.rotation).build()
        cameraPreview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    private fun bindInputAnalyser() {
        imageAnalysis =
            ImageAnalysis.Builder().setTargetRotation(binding.cameraPreview.display.rotation)
                .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(imageProxy)
        }

        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(imageProxy: ImageProxy) {
        TFLiteModelExecutor.executeTensorModel(lifecycleScope, this,imageProxy.toBitmap())

        faceDetectionRepository.processImageProxy(imageProxy, ::onSuccess, ::onFailure)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun onSuccess(faces: List<Face>, imageProxy: ImageProxy) {
        // Handle the case where no faces are detected
        binding.graphicOverlay.clear()
        if (faces.isEmpty()) {
            unBindFaceView()
            // ...handle no faces scenario...
            return
        }

        val biggestFace = biggestFace(faces)!!
        bindFaceView(biggestFace)
        val faceBox =
            FaceBox(binding.graphicOverlay, biggestFace, imageProxy.cropRect, imageProxy.toBitmap())

        val cropToBBox = cropToBBox(
            imageProxy.toBitmap(),
            biggestFace.boundingBox,
            imageProxy.imageInfo.rotationDegrees
        )
        binding.faceInfo.ivWork.setImageBitmap(
            cropToBBox?.flip(horizontal = true)?.getOrNull()
        )

        binding.graphicOverlay.add(faceBox)
//
//        val bitmapBuffer = BitmapUtils.createBitmap(imageProxy.width, imageProxy.height)
//        bitmapBuffer.convertBitmapToByteBuffer(inputBuffer)
//        BitmapUtils.runInference(bitmapBuffer, interpreter, inputBuffer)
        cropToBBox?.let {
            TFLiteModelExecutor.executeTensorModel(lifecycleScope, this,it)
        }

    }

    private fun cropToBBox(image: Bitmap, boundingBox: Rect, rotation: Int): Bitmap? {
        var image = image
        val shift = 0
        if (rotation != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            image = Bitmap.createBitmap(image, 0, 0, image.width, image.height, matrix, true)
        }
        return if (boundingBox.top >= 0 && boundingBox.bottom <= image.width && boundingBox.top + boundingBox.height() <= image.height && boundingBox.left >= 0 && boundingBox.left + boundingBox.width() <= image.width) {
            Bitmap.createBitmap(
                image,
                boundingBox.left,
                boundingBox.top + shift,
                boundingBox.width(),
                boundingBox.height()
            )
        } else null
    }

    private fun onFailure(exception: Exception) {
        Log.e(TAG, exception.message ?: "Face Detection Failed")
    }

    private fun biggestFace(faces: List<Face>): Face? {
        var biggestFace: Face? = null
        var biggestFaceSize = 0
        for (face in faces) {
            val faceSize = face.boundingBox.height() * face.boundingBox.width()
            if (faceSize > biggestFaceSize) {
                biggestFaceSize = faceSize
                biggestFace = face
            }
        }
        return biggestFace
    }

    private fun bindFaceView(currenctFace: Face) {
        binding.faceInfo.apply {
            txtSimilarityValue.text = "Nan%"
            txtDistanceValue.text = "0.00"
            txtRealProbabilityValue.text = "0.00"
            txtSmilingValue.text =
                ((currenctFace.smilingProbability ?: 0).toFloat() * 100).toInt()
                    .toString() + " %"
            txtLeftEyeValue.text =
                ((currenctFace.leftEyeOpenProbability ?: 0).toFloat() * 100).toInt()
                    .toString() + " %"
            txtRightEyeValue.text =
                ((currenctFace.rightEyeOpenProbability ?: 0).toFloat() * 100).toInt()
                    .toString() + " %"
            txtHeadRotationXValue.text = String.format("%.2f", currenctFace.headEulerAngleX)
            txtHeadRotationYValue.text = String.format("%.2f", currenctFace.headEulerAngleY)
            txtHeadRotationZValue.text = String.format("%.2f", currenctFace.headEulerAngleZ)
        }
    }

    private fun unBindFaceView() {
        binding.faceInfo.apply {
            txtSimilarityValue.text = "Nan%"
            txtDistanceValue.text = "0.00"
            txtRealProbabilityValue.text = "0.00"
            txtSmilingValue.text = "0.00"
            txtLeftEyeValue.text = "0.00"
            txtRightEyeValue.text = "0.00"
            txtHeadRotationXValue.text = "0.00"
            txtHeadRotationYValue.text = "0.00"
            txtHeadRotationZValue.text = "0.00"
        }
    }

    companion object {
        private val TAG = FaceDetectionActivity::class.simpleName
    }


}