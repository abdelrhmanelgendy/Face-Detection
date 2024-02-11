package net.gamal.faceapprecon.detection.domain.models

import android.graphics.Bitmap

data class EncodedFaceInformation(
    val id: Int = -1, val name: String, val faceEmbedding: FloatArray, var faceImage: Bitmap? = null
) {
    constructor(faceEmbedding: FloatArray) : this(-1, "", faceEmbedding)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncodedFaceInformation

        if (id != other.id) return false
        if (name != other.name) return false
        if (!faceEmbedding.contentEquals(other.faceEmbedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + faceEmbedding.contentHashCode()
        return result
    }
}