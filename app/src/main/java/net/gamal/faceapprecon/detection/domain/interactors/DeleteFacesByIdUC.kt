package net.gamal.faceapprecon.detection.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.gamal.faceapprecon.detection.data.repository.EncodedFaceRepository
import net.gamal.faceapprecon.utilities.interactor.UseCaseLocal
import javax.inject.Inject

class DeleteFacesByIdUC @Inject constructor(private val repo: EncodedFaceRepository) :
    UseCaseLocal<Unit, Int>() {

    override fun executeLocalDS(body: Int?): Flow<Unit> = flow {
        emit(repo.deleteFaceById(body!!))
    }

}