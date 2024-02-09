package net.gamal.faceapprecon.facedetection.presentation

import android.app.Application
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.face.Face
import net.gamal.faceapprecon.facedetection.data.FaceDetectionRepository
import net.gamal.faceapprecon.facedetection.data.model.EncodedFaceInformation
import kotlin.math.sqrt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CameraXViewModel(private val application: Application) : AndroidViewModel(application) {

    private var savedFaces: List<EncodedFaceInformation> = listOf()
    private val faceDetectionRepository = FaceDetectionRepository(application)

    val processCameraProvider: LiveData<ProcessCameraProvider> by lazy {
        faceDetectionRepository.initializeCamera()
    }



    fun bindCameraPreview(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        faceDetectionRepository.bindCameraPreview(previewView, lifecycleOwner = lifecycleOwner)
    }

    @ExperimentalGetImage
    fun bindInputAnalyser(
        lifecycleOwner: LifecycleOwner,
        imaPreviewView: PreviewView,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        faceDetectionRepository.bindInputAnalyser(
            lifecycleOwner, imaPreviewView, onSuccess, onFailure
        )
    }

    @ExperimentalGetImage
    fun switchCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onSuccess: (List<Face>, ImageProxy) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        faceDetectionRepository.switchCamera(previewView, lifecycleOwner, onSuccess, onFailure)
    }

    fun getCurrentCamera(): Int {
        return faceDetectionRepository.getCurrentCamera()
    }

    private val faces_data = "faces_data"

    fun saveFace(name: String, it: FloatArray,faceBitmap: Bitmap) {
        val sharedPreferences = application.getSharedPreferences("faces", Context.MODE_PRIVATE)

        val allFaces = getAllFaces()
        val facesMutableList = allFaces.toMutableList()
        facesMutableList.add(EncodedFaceInformation(name, it))
        sharedPreferences.edit().putString(faces_data, Gson().toJson(facesMutableList)).apply()
        saveBitmapToCacheDirectory(application, faceBitmap, "$name.png")
        this.savedFaces = facesMutableList
    }


    private fun saveBitmapToCacheDirectory(context: Context, bitmap: Bitmap, fileName: String): File? {
        val cacheDir = context.cacheDir // Get the cache directory
        val file = File(cacheDir, fileName) // Create a new file in the cache directory

        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos) // Compress bitmap to PNG format and write it to the file
                fos.flush()
            }
            return file // Return the saved file
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun getBitmapFromCacheDirectory(context: Context, fileName: String): Bitmap? {
        val cacheDir = context.cacheDir // Get the cache directory
        val file = File(cacheDir, fileName) // Create a file object with the specified filename

        if (file.exists()) {
            return BitmapFactory.decodeFile(file.absolutePath) // Decode the bitmap from the file
        }

        return null // Return null if the file does not exist
    }

    fun getAllFaces(): List<EncodedFaceInformation> {
        val sharedPreferences = application.getSharedPreferences("faces", Context.MODE_PRIVATE)

        val facesList = sharedPreferences.getString(faces_data, "-1")
        if (facesList != "-1") {
            val faces = Gson().fromJson<List<EncodedFaceInformation>>(
                facesList, object : TypeToken<List<EncodedFaceInformation>>() {}.type
            )
            return faces
        }
        return emptyList()
    }

    fun recognizeFace(currentFace: FloatArray): Pair<EncodedFaceInformation, Bitmap>? {
        if(savedFaces.isEmpty())
        {
            this.savedFaces = getAllFaces()
        }
        val encodedSavedFaceData = savedFaces.map {
            val restoredFloatArray = it.faceEmbedding
            EncodedFaceInformation(it.name, restoredFloatArray)
        }.toList()
        val nearestFace = findNearestFace(currentFace, encodedSavedFaceData)
        if (nearestFace != null) {
            println("Recognized face: ${nearestFace.name}")
            return Pair(nearestFace, getBitmapFromCacheDirectory(application, "${nearestFace.name}.png")!!)
        } else {
            println("Recognized face: Face not found in the database.")
        }
        return null
    }

    private fun findNearestFace(
        embedding1: FloatArray,
        embeddingList: List<EncodedFaceInformation>
    ): EncodedFaceInformation? {
        // Normalize current face embedding
        val norm1 = sqrt(embedding1.sumByDouble { it.toDouble() * it }.toFloat())
        val normalizedEmbedding1 = embedding1.map { it / norm1 }.toFloatArray()


        var nearestEncodedFace: EncodedFaceInformation? = null
        var minDistance = Float.MAX_VALUE

        // Iterate through the list of face embeddings
        for ((index, embedding2) in embeddingList.withIndex()) {
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
        if (minDistance <= 0.9) {
            return nearestEncodedFace
        } else {
            return null // No face found within the threshold
        }
    }

}