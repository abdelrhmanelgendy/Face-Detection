package net.gamal.faceapprecon.facedetextion.data


import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetectionRepository {
    private val detector: FaceDetector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder().setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build()
        )
    }

    fun processImageProxy(
        imageProxy: ImageProxy,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val inputImage =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        detector.process(inputImage).addOnSuccessListener { onSuccess(it, imageProxy) }
            .addOnFailureListener { onFailure(it) }.addOnCompleteListener { imageProxy.close() }
    }
}
