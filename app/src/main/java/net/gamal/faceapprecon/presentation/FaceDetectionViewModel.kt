package net.gamal.faceapprecon.presentation

import android.app.Application
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import net.gamal.faceapprecon.detection.data.repository.FaceDetectionRepository
import net.gamal.faceapprecon.detection.domain.interactors.DeleteFacesByIdUC
import net.gamal.faceapprecon.detection.domain.interactors.GetAllFacesUC
import net.gamal.faceapprecon.detection.domain.interactors.InsertFaceUC
import net.gamal.faceapprecon.detection.domain.models.EncodedFaceInformation
import net.gamal.faceapprecon.utilities.utils.Resource
import javax.inject.Inject

@HiltViewModel
class FaceDetectionViewModel @Inject constructor(
    application: Application,
    private val insertFaceUC: InsertFaceUC,
    private val deleteFacesByIdUC: DeleteFacesByIdUC,
    private val getAllFacesUC: GetAllFacesUC,
    private val getFacesByIdUC: DeleteFacesByIdUC
) : AndroidViewModel(application) {
    @OptIn(ExperimentalGetImage::class)
    fun startDetection(
        proxy: ImageProxy,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FaceDetectionRepository.processImageProxy(proxy, onSuccess, onFailure)
    }

    fun insertFace(encodedFace: EncodedFaceInformation) {
        insertFaceUC.invoke(viewModelScope,encodedFace){result->
            when(result)
            {
                is Resource.Failure -> println("insertFace:: Failed to insert face ${result.exception}")
                is Resource.Progress -> println("insertFace:: Inserting face ${result.loading}")
                is Resource.Success -> println("insertFace:: Face inserted successfully")
            }
        }
    }
}