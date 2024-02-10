package net.gamal.faceapprecon.detection.domain.repository.localDs

import net.gamal.faceapprecon.detection.data.models.entity.EncodedFaceInformationEntity

interface IEncodedFaceLocalDS {
    suspend fun insertFace(face:EncodedFaceInformationEntity)
    suspend fun getAllFaces():List<EncodedFaceInformationEntity>
    suspend fun getFaceById(id:Int):EncodedFaceInformationEntity
    suspend fun deleteFaceById(id:Int)
}