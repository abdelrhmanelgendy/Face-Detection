package net.gamal.faceapprecon.detection.presentation.mvi

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.gamal.faceapprecon.detection.data.repository.FaceDetectionRepository
import net.gamal.faceapprecon.detection.data.repository.LocalBitmapRepository
import net.gamal.faceapprecon.detection.domain.interactors.DeleteFacesByIdUC
import net.gamal.faceapprecon.detection.domain.interactors.GetAllFacesUC
import net.gamal.faceapprecon.detection.domain.interactors.GetFacesByIdUC
import net.gamal.faceapprecon.detection.domain.interactors.InsertFaceUC
import net.gamal.faceapprecon.detection.domain.models.EncodedFaceInformation
import net.gamal.faceapprecon.utilities.ml.models.EncodedFaceModelExecutor
import net.gamal.faceapprecon.utilities.ml.models.TFLiteModelExecutor
import net.gamal.faceapprecon.utilities.utils.FaceRecognitionUtils
import net.gamal.faceapprecon.utilities.utils.Resource
import net.gamal.faceapprecon.utilities.viewModel.BaseViewModel
import net.gamal.faceapprecon.utilities.viewModel.ViewAction
import javax.inject.Inject

@HiltViewModel
class FaceDetectionViewModel @Inject constructor(
    application: Application,
    private val localBitmapRepository: LocalBitmapRepository,
    private val insertFaceUC: InsertFaceUC,
    private val deleteFacesByIdUC: DeleteFacesByIdUC,
    private val getAllFacesUC: GetAllFacesUC,
    private val getFacesByIdUC: GetFacesByIdUC,
    private val encodedFaceModelExecutor: EncodedFaceModelExecutor,
    private val tFLiteModelExecutor: TFLiteModelExecutor,
) : BaseViewModel<FaceDetectionContract.FaceDetectionAction, FaceDetectionContract.FaceDetectionEvent, FaceDetectionContract.FaceDetectionState>(
    FaceDetectionContract.FaceDetectionState.initial()
) {
    private var allFaces: List<EncodedFaceInformation> = emptyList()
    override fun onActionTrigger(action: ViewAction?) {
        setState(oldViewState.copy(action = action, exception = null, isLoading = false))
        when (action) {
            is FaceDetectionContract.FaceDetectionAction.InsertFaceData -> insertFace(
                action.encodedFaceInformation, action.bitmap
            )

            is FaceDetectionContract.FaceDetectionAction.FetchListOfFaceDetections -> getAllFaces(
                action.requireImages
            )

            is FaceDetectionContract.FaceDetectionAction.FetchFaceDataByID -> getFacesById(action.id)
            is FaceDetectionContract.FaceDetectionAction.DeleteFaceDataByID -> deleteFacesById(
                action.id
            )

            is FaceDetectionContract.FaceDetectionAction.EncodeAndInsertFace -> encodingAndInsertingFace(
                action.name, action.bitmap, action.lifecycleScope, action.context
            )

            is FaceDetectionContract.FaceDetectionAction.StartDetection -> startDetection(
                action.imageProxy, action.onSuccess, action.onFailed
            )

            is FaceDetectionContract.FaceDetectionAction.EncodeAndFindFace -> encodeAndFindFace(
                action.lifecycleScope, action.context, action.bitmap
            )
        }
    }

    @OptIn(ExperimentalGetImage::class)
    fun startDetection(
        proxy: ImageProxy,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FaceDetectionRepository.processImageProxy(proxy, onSuccess, onFailure)
    }

    private fun insertFace(encodedFace: EncodedFaceInformation, faceBitmap: Bitmap) {
        println("insertFace:: Inserting face:: $encodedFace")
        insertFaceUC.invoke(viewModelScope, encodedFace) { result ->
            when (result) {
                is Resource.Failure -> {
                    println("insertFace:: Failure:: ${result.exception}")
                    setState(oldViewState.copy(exception = result.exception))
                }

                is Resource.Progress -> {
                    setState(oldViewState.copy(isLoading = result.loading))
                }

                is Resource.Success -> {
                    println("insertFace:: Failure:: Face inserted successfully")
                    localBitmapRepository.saveBitmapToCacheDirectory(faceBitmap, encodedFace.name)
                    sendEvent(FaceDetectionContract.FaceDetectionEvent.FaceInsertedSuccessfully)
                    processIntent(
                        FaceDetectionContract.FaceDetectionAction.FetchListOfFaceDetections(
                            false
                        )
                    )
                }
            }
        }
    }

    private fun getAllFaces(requireImages: Boolean) {
        getAllFacesUC.invoke(viewModelScope, Unit) { result ->
            when (result) {
                is Resource.Failure -> {
                    setState(oldViewState.copy(exception = result.exception))
                }

                is Resource.Progress -> {
                    setState(oldViewState.copy(isLoading = result.loading))
                }

                is Resource.Success -> {
                    allFaces = result.model
                    if (requireImages) {
                        allFaces.forEach {
                            it.faceImage =
                                localBitmapRepository.getSavedFileFromInternalCache(it.name)
                        }
                    }
                    sendEvent(FaceDetectionContract.FaceDetectionEvent.FetchedListOfFaces(allFaces))
                }
            }
        }
    }

    private fun deleteFacesById(id: Int) {
        deleteFacesByIdUC.invoke(viewModelScope, id) { result ->
            when (result) {
                is Resource.Failure -> {
                    setState(oldViewState.copy(exception = result.exception))
                }

                is Resource.Progress -> {
                    setState(oldViewState.copy(isLoading = result.loading))
                }

                is Resource.Success -> {
                    println("deleteFacesById:: Face deleted successfully")
                    getAllFaces(true)
                }
            }
        }
    }

    private fun getFacesById(id: Int) {
        getFacesByIdUC.invoke(viewModelScope, id) { result ->
            when (result) {
                is Resource.Failure -> {
                    setState(oldViewState.copy(exception = result.exception))
                }

                is Resource.Progress -> {
                    setState(oldViewState.copy(isLoading = result.loading))
                }

                is Resource.Success -> {
                    sendEvent(FaceDetectionContract.FaceDetectionEvent.FetchedFaceByID(result.model))
                }
            }
        }
    }

    private fun encodingAndInsertingFace(
        name: String, faceBitmap: Bitmap, lifecycleOwner: LifecycleCoroutineScope, context: Context
    ) {
        encodeFace(lifecycleOwner, context, faceBitmap) {
            insertFace(EncodedFaceInformation(name = name, faceEmbedding = it), faceBitmap)
        }
    }

    private fun encodeAndFindFace(
        lifecycle: LifecycleCoroutineScope, context: Context, bitmap: Bitmap
    ) {
        lifecycle.launch(Dispatchers.Default) {
            encodeFace(lifecycle, context, bitmap) {
                val nearestFaceResult =
                    FaceRecognitionUtils.findNearestFace(EncodedFaceInformation(it), allFaces)
                nearestFaceResult?.let {
                    loadImageByName(it)
                }
            }
        }
    }

    private fun loadImageByName(it: EncodedFaceInformation) {
        it.faceImage = localBitmapRepository.getSavedFileFromInternalCache(it.name)
        sendEvent(FaceDetectionContract.FaceDetectionEvent.FaceRecognizedSuccessfully(it))
    }


    private fun encodeFace(
        lifecycle: LifecycleCoroutineScope,
        context: Context,
        bitmap: Bitmap,
        onSuccess: (FloatArray) -> Unit
    ) {
        viewModelScope.launch {
            tFLiteModelExecutor.executeTensorModel(lifecycle, bitmap) {
                onSuccess(it)
            }
        }
    }

    override fun clearState() {

    }
}