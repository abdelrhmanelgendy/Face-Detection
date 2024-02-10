package net.gamal.faceapprecon.detection.datasource.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import net.gamal.faceapprecon.detection.data.models.entity.EncodedFaceInformationEntity

class EncodedFaceInformationEntityTypeConverter {
    @TypeConverter
    fun fromEncodedFace(face: EncodedFaceInformationEntity): String {
        val gson = Gson()
        return gson.toJson(face)
    }

    @TypeConverter
    fun toEncodedFace(face: String): EncodedFaceInformationEntity {
        val gson = Gson()
        return gson.fromJson(face, EncodedFaceInformationEntity::class.java)
    }

    @TypeConverter
    fun fromFloatArray(floatArray: FloatArray): String {
        val gson = Gson()
        return gson.toJson(floatArray)
    }

    @TypeConverter
    fun toFloatArray(floatArray: String): FloatArray {
        val gson = Gson()
        return gson.fromJson(floatArray, FloatArray::class.java)
    }
}