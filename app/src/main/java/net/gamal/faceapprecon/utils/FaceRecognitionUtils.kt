package net.gamal.faceapprecon.utils

import net.gamal.faceapprecon.detection.domain.models.EncodedFaceInformation
import kotlin.math.sqrt

object FaceRecognitionUtils {
    fun findNearestFace(
        currentFace: EncodedFaceInformation, allFaces: List<EncodedFaceInformation>
    ): EncodedFaceInformation? {
        // Normalize current face embedding
        val embedding1 = currentFace.faceEmbedding
        val norm1 = sqrt(embedding1.sumByDouble { it.toDouble() * it }.toFloat())
        val normalizedEmbedding1 = embedding1.map { it / norm1 }.toFloatArray()


        var nearestEncodedFace: EncodedFaceInformation? = null
        var minDistance = Float.MAX_VALUE

        // Iterate through the list of face embeddings
        for ((index, embedding2) in allFaces.withIndex()) {
            // Normalize face embedding from the list
            val norm2 = sqrt(embedding2.faceEmbedding.sumByDouble { it.toDouble() * it }.toFloat())
            val normalizedEmbedding2 = embedding2.faceEmbedding.map { it / norm2 }.toFloatArray()

            // Compute Euclidean distance between current face and face from the list
            var sum = 0.0f
            for (i in normalizedEmbedding1.indices) {
                val diff = normalizedEmbedding1[i] - normalizedEmbedding2[i]
                sum += diff * diff
            }
            val distance = sqrt(sum)

            // Update nearest face index and distance if closer than previous closest face
            if (distance < minDistance) {
                nearestEncodedFace = embedding2
                minDistance = distance
            }
        }

        // Check if the minimum distance is within the threshold
        return if (minDistance <= 0.9) {
            nearestEncodedFace
        } else {
            null // No face found within the threshold
        }
    }
}