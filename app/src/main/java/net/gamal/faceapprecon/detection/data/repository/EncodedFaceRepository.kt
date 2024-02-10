package net.gamal.faceapprecon.detection.data.repository

import net.gamal.faceapprecon.detection.data.mapper.EncodedFaceInformationMapper
import net.gamal.faceapprecon.detection.data.repository.localDs.EncodedFaceLocalDS
import net.gamal.faceapprecon.detection.domain.models.EncodedFaceInformation
import net.gamal.faceapprecon.detection.domain.repository.IEncodedFaceRepository
import javax.inject.Inject

class EncodedFaceRepository @Inject constructor(private val localDS: EncodedFaceLocalDS) :
    IEncodedFaceRepository {
    override suspend fun insertFace(face: EncodedFaceInformation) {
        EncodedFaceInformationMapper.domainToEntity(face).apply {
            localDS.insertFace(this)
        }
    }

    override suspend fun getAllFaces(): List<EncodedFaceInformation> {
        val result = localDS.getAllFaces()
        return result.map {
            EncodedFaceInformationMapper.entityToDomain(it)
        }
    }

    override suspend fun getFaceByID(id: Int): EncodedFaceInformation {
        val result = localDS.getFaceById(id)
        return EncodedFaceInformationMapper.entityToDomain(result)
    }

    override suspend fun deleteFaceById(id: Int) {
        localDS.deleteFaceById(id)
    }

}