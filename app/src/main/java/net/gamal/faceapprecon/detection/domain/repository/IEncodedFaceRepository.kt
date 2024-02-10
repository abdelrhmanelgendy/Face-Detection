package net.gamal.faceapprecon.detection.domain.repository

import net.gamal.faceapprecon.detection.domain.models.EncodedFaceInformation

interface IEncodedFaceRepository {
    suspend fun insertFace(face: EncodedFaceInformation)
    suspend fun getAllFaces(): List<EncodedFaceInformation>
    suspend fun getFaceByID(id:Int): EncodedFaceInformation
    suspend fun deleteFaceById(id:Int)
}