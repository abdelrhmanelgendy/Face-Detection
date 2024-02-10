package net.gamal.faceapprecon.detection.datasource.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.gamal.faceapprecon.detection.data.models.entity.EncodedFaceInformationEntity
import net.gamal.faceapprecon.detection.datasource.dao.EncodedFacesDAO
import net.gamal.faceapprecon.detection.datasource.typeconverter.EncodedFaceInformationEntityTypeConverter

@Database(entities = [EncodedFaceInformationEntity::class], version = 1)
@TypeConverters(EncodedFaceInformationEntityTypeConverter::class)
abstract class EncodedFacesDatabase : RoomDatabase() {
    public abstract fun encodedFaceDao(): EncodedFacesDAO
}