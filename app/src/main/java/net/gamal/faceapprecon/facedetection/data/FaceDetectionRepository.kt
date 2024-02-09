package net.gamal.faceapprecon.facedetection.data


import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class FaceDetectionRepository(private val context: Context) {
    private lateinit var cameraProvider: ProcessCameraProvider
    private var cameraSelector: CameraSelector
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private var currentCamera = CameraSelector.LENS_FACING_FRONT

    init {
        cameraSelector = CameraSelector.Builder().requireLensFacing(currentCamera).build()
    }

    private val detector: FaceDetector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder().setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build()
        )
    }

    @ExperimentalGetImage
    fun processImageProxy(
        imageProxy: ImageProxy,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val inputImage =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        detector.process(inputImage).addOnSuccessListener { onSuccess(it, imageProxy) }
            .addOnFailureListener { onFailure(it) }.addOnCompleteListener { imageProxy.close() }
    }


    fun initializeCamera(): LiveData<ProcessCameraProvider> {
        val cameraProviderLiveData: MutableLiveData<ProcessCameraProvider> = MutableLiveData()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                try {
                    cameraProvider = cameraProviderFuture.get()
                    cameraProviderLiveData.postValue(cameraProvider)
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context)
        )
        return cameraProviderLiveData
    }

    fun bindCameraPreview(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        cameraPreview = Preview.Builder().setTargetRotation(previewView.display.rotation).build()
        cameraPreview.setSurfaceProvider(previewView.surfaceProvider)
        try {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, cameraPreview)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    @ExperimentalGetImage
    fun bindInputAnalyser(
        lifecycleOwner: LifecycleOwner,
        imaPreviewView: PreviewView,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        imageAnalysis =
            ImageAnalysis.Builder().setTargetRotation(imaPreviewView.display.rotation).build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(imageProxy, onSuccess, onFailure)
        }

        try {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    @ExperimentalGetImage
    fun switchCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        unbindCamera()
        currentCamera = if (currentCamera == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(currentCamera).build()
        bindCameraPreview(previewView, lifecycleOwner)
        bindInputAnalyser(lifecycleOwner, previewView, onSuccess, onFailure)
    }

    private fun unbindCamera() {
        cameraProvider.unbindAll()
    }

    fun getCurrentCamera(): Int {
        return currentCamera
    }

    companion object {
        private const val TAG = "FaceDetectionRepository"
    }
}
