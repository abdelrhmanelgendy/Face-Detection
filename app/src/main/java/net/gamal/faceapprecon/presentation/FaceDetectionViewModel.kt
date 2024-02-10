package net.gamal.faceapprecon.presentation

import android.app.Application
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import net.gamal.faceapprecon.detection.data.repository.FaceDetectionRepository
import javax.inject.Inject

@HiltViewModel
class FaceDetectionViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {


    @OptIn(ExperimentalGetImage::class)
    fun startDetection(
        proxy: ImageProxy,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FaceDetectionRepository.processImageProxy(proxy, onSuccess, onFailure)
    }
}