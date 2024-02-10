package net.gamal.faceapprecon.detection.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.gamal.faceapprecon.detection.data.repository.EncodedFaceRepository
import net.gamal.faceapprecon.detection.domain.models.EncodedFaceInformation
import net.gamal.faceapprecon.utilities.interactor.UseCaseLocal
import net.gamal.faceapprecon.utils.FaceRecognitionUtils
import javax.inject.Inject

class InsertFaceUC @Inject constructor(private val repo: EncodedFaceRepository) :
    UseCaseLocal<Unit, EncodedFaceInformation>() {

    override fun executeLocalDS(body: EncodedFaceInformation?): Flow<Unit> = flow {
        if (body != null) {
            if (checkBodyExists(body)) {
                throw Exception("Face already exists")
            } else {
                emit(repo.insertFace(body))
            }
        }
    }

    private suspend fun checkBodyExists(body: EncodedFaceInformation): Boolean {
        val allFaces = repo.getAllFaces()
        val nearestFaceResult = FaceRecognitionUtils.findNearestFace(body, allFaces)
        return nearestFaceResult == null
    }
}