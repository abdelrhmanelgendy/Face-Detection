package net.gamal.faceapprecon.detection.data.models.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people_faces")
data class EncodedFaceInformationEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int? = null,
    val name: String,
    val faceEmbedding: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncodedFaceInformationEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (!faceEmbedding.contentEquals(other.faceEmbedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * (result?:1) + name.hashCode()
        result = 31 * result + faceEmbedding.contentHashCode()
        return result
    }

}