package net.gamal.faceapprecon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.lifecycleScope
import net.gamal.faceapprecon.databinding.ActivityFaceDetectionBinding
import net.gamal.faceapprecon.ml.BitmapUtils
import net.gamal.faceapprecon.ml.BitmapUtils.convertBitmapToByteBuffer
import net.gamal.faceapprecon.ml.BitmapUtils.runInference
import net.gamal.faceapprecon.ml.TFLiteModelExecutor
import net.gamal.faceapprecon.ml.TFLiteModelLoader
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Executors

class FaceDetectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFaceDetectionBinding
    private lateinit var cameraSelector: CameraSelector
    private val cameraXViewModel by viewModels<CameraXViewModel>()
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var inputBuffer: ByteBuffer
    private lateinit var interpreter: Interpreter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        interpreter = Interpreter(TFLiteModelLoader.loadModelFile(assets, "model_unquant.tflite"))
        val inputSize = interpreter.getInputTensor(0).shape()[1]
        inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        inputBuffer.order(ByteOrder.nativeOrder())
        cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        cameraXViewModel.processCameraProvider.observe(this) { provider ->
            processCameraProvider = provider
            bindCameraPreview()
            bindInputAnalyser()
        }
    }

    private fun bindInputAnalyser() {
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(binding.cameraPreview.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        startAnalysis()
        try {
            processCameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                cameraPreview,
                imageAnalysis
            )
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startAnalysis() {
        val cameraExecutor = Executors.newSingleThreadExecutor()
        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmapBuffer = BitmapUtils.createBitmap(imageProxy.width, imageProxy.height)
            bitmapBuffer.convertBitmapToByteBuffer(inputBuffer)
            runInference(bitmapBuffer,interpreter, inputBuffer)
            TFLiteModelExecutor.executeTensorModel(lifecycleScope, this, inputBuffer)
            imageProxy.close()
        }
    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.cameraPreview.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    class BoundingBoxView(context: Context) : View(context) {
        private var rect: Rect? = null
        private val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        fun setBoundingBox(rect: Rect?) {
            this.rect = rect
            invalidate() // Redraw the view
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            rect?.let { canvas.drawRect(it, paint) }
        }
    }
    companion object {
        private val TAG = FaceDetectionActivity::class.simpleName.plus("SBS")
    }
}
