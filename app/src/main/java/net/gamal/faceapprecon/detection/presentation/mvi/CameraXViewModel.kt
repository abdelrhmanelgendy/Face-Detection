package net.gamal.faceapprecon.detection.presentation.mvi

import android.app.Application
import androidx.camera.core.Camera
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import net.gamal.faceapprecon.camera.repository.CameraXRepository
import javax.inject.Inject

class CameraXViewModel @Inject constructor(private val application: Application) :
    AndroidViewModel(application) {

    private val cameraXRepository = CameraXRepository(application)

    fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        cameraPreview: PreviewView,
        onCameraSet:(Camera)->Unit,
        onImageProxy: (ImageProxy) -> Unit
    ) {
        cameraXRepository.initializeCamera().observe(lifecycleOwner) {
            bindCameraPreview(cameraPreview, lifecycleOwner,onCameraSet,onImageProxy)
        }
    }

    private fun bindCameraPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onCameraSet:(Camera)->Unit,
        onImageProxy: (ImageProxy) -> Unit
    ) {
        cameraXRepository.bindCameraPreview(previewView, lifecycleOwner,onCameraSet, onImageProxy)
    }

    @ExperimentalGetImage
    fun switchCamera(
        previewView: PreviewView, lifecycleOwner: LifecycleOwner,onCameraSet:(Camera)->Unit ,onImageProxy: (ImageProxy) -> Unit
    ) {
        cameraXRepository.switchCamera(previewView, lifecycleOwner,onCameraSet, onImageProxy)
    }

    fun getCurrentCamera(): Int {
        return cameraXRepository.getCurrentCamera()
    }

    fun clear(){
        cameraXRepository.unbindCamera()
    }
}