package net.gamal.faceapprecon.detection.domain.models

data class EncodedFaceInformation(
    val id:Int,
    val name: String,
    val faceEmbedding: FloatArray
) {
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