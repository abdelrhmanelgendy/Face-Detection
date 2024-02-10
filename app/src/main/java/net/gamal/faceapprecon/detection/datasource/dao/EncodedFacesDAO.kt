package net.gamal.faceapprecon.detection.datasource.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import net.gamal.faceapprecon.detection.data.models.entity.EncodedFaceInformationEntity


@Dao
interface EncodedFacesDAO {
    @Insert
    suspend fun addNewFaceData(face: EncodedFaceInformationEntity):Unit

    @Query("Delete from people_faces where id =:id")
    suspend fun deleteFaceById(id: Int)

    @Query("Select * from people_faces")
    suspend fun getAllFaces(): List<EncodedFaceInformationEntity>

    @Query("Select * from people_faces where id=:id")
    suspend fun getFace(id:Int):EncodedFaceInformationEntity
}