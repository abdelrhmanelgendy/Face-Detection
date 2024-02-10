package net.gamal.faceapprecon.detection.data.mapper

import net.gamal.faceapprecon.detection.data.models.entity.EncodedFaceInformationEntity
import net.gamal.faceapprecon.detection.domain.models.EncodedFaceInformation

object EncodedFaceInformationMapper {
    fun entityToDomain(entity: EncodedFaceInformationEntity): EncodedFaceInformation = entity.run {
        EncodedFaceInformation(id!!, name, faceEmbedding)
    }

    fun domainToEntity(domain: EncodedFaceInformation): EncodedFaceInformationEntity = domain.run {
        EncodedFaceInformationEntity(name=domain.name,faceEmbedding= domain.faceEmbedding)
    }
}