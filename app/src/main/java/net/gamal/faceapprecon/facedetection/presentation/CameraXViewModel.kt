package net.gamal.faceapprecon.facedetection.presentation

import android.app.Application
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.mlkit.vision.face.Face
import net.gamal.faceapprecon.facedetection.data.FaceDetectionRepository

class CameraXViewModel(application: Application) : AndroidViewModel(application) {

    private val faceDetectionRepository = FaceDetectionRepository(application)

    val processCameraProvider: LiveData<ProcessCameraProvider> by lazy {
        faceDetectionRepository.initializeCamera()
    }

    fun bindCameraPreview(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        faceDetectionRepository.bindCameraPreview(previewView, lifecycleOwner = lifecycleOwner)
    }

    @ExperimentalGetImage
    fun bindInputAnalyser(
        lifecycleOwner: LifecycleOwner,
        imaPreviewView: PreviewView,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        faceDetectionRepository.bindInputAnalyser(
            lifecycleOwner,
            imaPreviewView,
            onSuccess,
            onFailure
        )
    }

    @ExperimentalGetImage
    fun switchCamera(
        previewView: PreviewView, lifecycleOwner: LifecycleOwner,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        faceDetectionRepository.switchCamera(previewView, lifecycleOwner, onSuccess, onFailure)
    }

    fun getCurrentCamera(): Int {
        return faceDetectionRepository.getCurrentCamera()
    }
}