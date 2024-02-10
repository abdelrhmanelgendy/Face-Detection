package net.gamal.faceapprecon.presentation

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
import net.gamal.faceapprecon.detection.data.models.entity.EncodedFaceInformationEntity
import kotlin.math.sqrt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import net.gamal.faceapprecon.camera.repository.CameraXRepository
import net.gamal.faceapprecon.utils.FaceRecognitionUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class CameraXViewModel @Inject constructor(private val application: Application) : AndroidViewModel(application) {

    private var savedFaces: List<EncodedFaceInformationEntity> = listOf()
    private val cameraXRepository = CameraXRepository(application)

    private val processCameraProvider: LiveData<ProcessCameraProvider> by lazy {
        cameraXRepository.initializeCamera()
    }

    fun setupCamera(lifecycleOwner: LifecycleOwner) {
        processCameraProvider.observe(lifecycleOwner) {
            bindCameraPreview(PreviewView(application), lifecycleOwner)
        }
    }
   private fun bindCameraPreview(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        cameraXRepository.bindCameraPreview(previewView, lifecycleOwner = lifecycleOwner)
    }

    @ExperimentalGetImage
    fun bindInputAnalyser(
        lifecycleOwner: LifecycleOwner,
        imaPreviewView: PreviewView,
        onImageProxy: (ImageProxy) -> Unit
    ) {
        cameraXRepository.bindInputAnalyser(
            lifecycleOwner, imaPreviewView,onImageProxy
        )
    }

    @ExperimentalGetImage
    fun switchCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onImageProxy: (ImageProxy) -> Unit
    ) {
        cameraXRepository.switchCamera(previewView, lifecycleOwner,onImageProxy)
    }

    fun getCurrentCamera(): Int {
        return cameraXRepository.getCurrentCamera()
    }

    private val faces_data = "faces_data"

    fun saveFace(name: String, it: FloatArray,faceBitmap: Bitmap) {
        val sharedPreferences = application.getSharedPreferences("faces", Context.MODE_PRIVATE)

        val allFaces = getAllFaces()
        val facesMutableList = allFaces.toMutableList()
        facesMutableList.add(EncodedFaceInformationEntity(name, it))
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

    fun getAllFaces(): List<EncodedFaceInformationEntity> {
        val sharedPreferences = application.getSharedPreferences("faces", Context.MODE_PRIVATE)

        val facesList = sharedPreferences.getString(faces_data, "-1")
        if (facesList != "-1") {
            val faces = Gson().fromJson<List<EncodedFaceInformationEntity>>(
                facesList, object : TypeToken<List<EncodedFaceInformationEntity>>() {}.type
            )
            return faces
        }
        return emptyList()
    }

    fun recognizeFace(currentFace: FloatArray): Pair<EncodedFaceInformationEntity, Bitmap>? {
        if(savedFaces.isEmpty())
        {
            this.savedFaces = getAllFaces()
        }
        val encodedSavedFaceData = savedFaces.map {
            val restoredFloatArray = it.faceEmbedding
            EncodedFaceInformationEntity(it.name, restoredFloatArray)
        }.toList()
        val nearestFace = FaceRecognitionUtils.findNearestFace(currentFace, encodedSavedFaceData)
        if (nearestFace != null) {
            println("Recognized face: ${nearestFace.name}")
            return Pair(nearestFace, getBitmapFromCacheDirectory(application, "${nearestFace.name}.png")?:Bitmap.createBitmap(112,112,Bitmap.Config.ARGB_8888))
        } else {
            println("Recognized face: Face not found in the database.")
        }
        return null
    }



}