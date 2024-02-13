package net.gamal.faceapprecon.detection.presentation.mvi

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.mlkit.vision.face.Face
import net.gamal.faceapprecon.detection.domain.models.EncodedFaceInformation
import net.gamal.faceapprecon.utilities.viewModel.ViewAction
import net.gamal.faceapprecon.utilities.viewModel.ViewEvent
import net.gamal.faceapprecon.utilities.viewModel.ViewState
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2

interface FaceDetectionContract {

    sealed class FaceDetectionAction : ViewAction {
        data class InsertFaceData(
            val encodedFaceInformation: EncodedFaceInformation,
            val bitmap: Bitmap
        ) :
            FaceDetectionAction()

        data class DeleteFaceDataByID(val id: Int) : FaceDetectionAction()
        data class FetchListOfFaceDetections(val requireImages: Boolean = false) :
            FaceDetectionAction()

        data class FetchFaceDataByID(val id: Int) : FaceDetectionAction()
        data class EncodeAndInsertFace(
            val name: String,
            val bitmap: Bitmap,
            val lifecycleScope: LifecycleCoroutineScope,
            val context: Context
        ) : FaceDetectionAction()

        data class EncodeAndFindFace(
            val lifecycleScope: LifecycleCoroutineScope, val context: Context, val bitmap: Bitmap
        ) : FaceDetectionAction()

        class StartDetection(
            val imageProxy: ImageProxy,
            val onSuccess: KFunction2<List<Face>, ImageProxy, Unit>,
            val onFailed: KFunction1<Exception, Unit>
        ) : FaceDetectionAction()
    }

    sealed class FaceDetectionEvent : ViewEvent {
        data class FetchedListOfFaces(val faces: List<EncodedFaceInformation>) :
            FaceDetectionEvent()
        data object FaceNotFound : FaceDetectionEvent()

        data class FetchedFaceByID(val face: EncodedFaceInformation) : FaceDetectionEvent()

        data object FaceInsertedSuccessfully : FaceDetectionEvent()
        data class FaceRecognizedSuccessfully(val recognizedFace: EncodedFaceInformation) :
            FaceDetectionEvent()
    }

    data class FaceDetectionState(
        val isLoading: Boolean, val exception: Exception?, val action: ViewAction?

    ) : ViewState {
        companion object {
            fun initial() = FaceDetectionState(
                isLoading = false, exception = null, action = null
            )
        }
    }
}