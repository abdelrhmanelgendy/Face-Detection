package net.gamal.faceapprecon.camera.repository


import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
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
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class CameraXRepository(private val context: Context) {
    private lateinit var cameraProvider: ProcessCameraProvider
    private var cameraSelector: CameraSelector
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    private var currentCamera = CameraSelector.LENS_FACING_FRONT

    init {
        cameraSelector = CameraSelector.Builder().requireLensFacing(currentCamera).build()
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

    @OptIn(ExperimentalGetImage::class)
    fun bindCameraPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onImageProxy: (ImageProxy) -> Unit

    ) {
        cameraPreview = Preview.Builder().setTargetRotation(previewView.display.rotation).build()
        cameraPreview.setSurfaceProvider(previewView.surfaceProvider)
        try {
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, cameraPreview)
            bindInputAnalyser(lifecycleOwner, previewView,onImageProxy)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: "IllegalStateException")
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: "IllegalArgumentException")
        }
    }

    @ExperimentalGetImage
    private fun bindInputAnalyser(
        lifecycleOwner: LifecycleOwner,
        imaPreviewView: PreviewView,
        onImageProxy: (ImageProxy) -> Unit
    ) {
        imageAnalysis =
            ImageAnalysis.Builder().setTargetRotation(imaPreviewView.display.rotation).build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            onImageProxy(imageProxy)
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
        previewView: PreviewView, lifecycleOwner: LifecycleOwner, onImageProxy: (ImageProxy) -> Unit
    ) {
        unbindCamera()
        currentCamera = if (currentCamera == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        cameraSelector = CameraSelector.Builder().requireLensFacing(currentCamera).build()
        bindCameraPreview(previewView, lifecycleOwner,onImageProxy)
    }

    private fun unbindCamera() {
        cameraProvider.unbindAll()
    }

    fun getCurrentCamera(): Int {
        return currentCamera
    }

    companion object {
        private const val TAG = "CameraXRepository"
    }
}
