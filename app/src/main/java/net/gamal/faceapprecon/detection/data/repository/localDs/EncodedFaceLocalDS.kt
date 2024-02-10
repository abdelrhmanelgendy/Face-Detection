package net.gamal.faceapprecon.detection.data.repository.localDs

import net.gamal.faceapprecon.detection.data.models.entity.EncodedFaceInformationEntity
import net.gamal.faceapprecon.detection.datasource.dao.EncodedFacesDAO
import net.gamal.faceapprecon.detection.domain.repository.localDs.IEncodedFaceLocalDS
import javax.inject.Inject

class EncodedFaceLocalDS @Inject constructor(private val faceDao: EncodedFacesDAO) : IEncodedFaceLocalDS {

    override suspend fun insertFace(face: EncodedFaceInformationEntity) {
        faceDao.addNewFaceData(face)
    }

    override suspend fun getAllFaces(): List<EncodedFaceInformationEntity> {
        return faceDao.getAllFaces()
    }

    override suspend fun getFaceById(id: Int): EncodedFaceInformationEntity {
        return faceDao.getFace(id)
    }

    override suspend fun deleteFaceById(id: Int) {
         faceDao.deleteFaceById(id)
    }

}