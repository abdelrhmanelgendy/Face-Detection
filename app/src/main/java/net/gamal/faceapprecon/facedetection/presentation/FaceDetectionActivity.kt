package net.gamal.faceapprecon.facedetection.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.face.Face
import net.gamal.faceapprecon.databinding.ActivityFaceDetectionBinding
import net.gamal.faceapprecon.facedetection.data.model.FaceBox
import net.gamal.faceapprecon.ml.TFLiteModelExecutor
import net.gamal.faceapprecon.utils.ImageDetectorUtil
import net.gamal.faceapprecon.utils.MediaUtils.flip


class FaceDetectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceDetectionBinding
    private val cameraXViewModel by viewModels<CameraXViewModel>()

    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupCamera()
        setupSwitchCameraButton()
    }

    @ExperimentalGetImage
    private fun setupCamera() {
        cameraXViewModel.processCameraProvider.observe(this) {
            cameraXViewModel.bindCameraPreview(binding.cameraPreview, this)
            cameraXViewModel.bindInputAnalyser(
                this,
                binding.cameraPreview,
                ::onSuccess,
                ::onFailure
            )
        }
    }

    @ExperimentalGetImage
    private fun setupSwitchCameraButton() {
        binding.switchCamera.setOnClickListener {
            cameraXViewModel.switchCamera(
                binding.cameraPreview, this, ::onSuccess, ::onFailure
            )
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun onSuccess(faces: List<Face>, imageProxy: ImageProxy) {
        // Handle the case where no faces are detected
        binding.graphicOverlay.clear()
        if (faces.isEmpty()) {
//            unBindFaceView()
            // ...handle no faces scenario...
            return
        }

        val biggestFace = ImageDetectorUtil.biggestFace(faces)!!
        bindFaceView(biggestFace)


        val cropToBBox = ImageDetectorUtil.cropFaceToBBox(
            imageProxy.toBitmap(), biggestFace.boundingBox, imageProxy.imageInfo.rotationDegrees
        )
        val faceBox = FaceBox(
            binding.graphicOverlay,
            biggestFace,
            imageProxy.cropRect,
            (cameraXViewModel.getCurrentCamera() == CameraSelector.LENS_FACING_FRONT).not()
        )
        binding.faceInfo.ivWork.setImageBitmap(
            if (cameraXViewModel.getCurrentCamera() == CameraSelector.LENS_FACING_FRONT) {
                cropToBBox?.flip(horizontal = true)?.getOrNull()
            } else {
                cropToBBox
            }
        )
        binding.graphicOverlay.add(faceBox)
        cropToBBox?.let {
            TFLiteModelExecutor.executeTensorModel(lifecycleScope, this, it){

            }
        }
    }

    private fun onFailure(exception: Exception) {
        Log.e(TAG, exception.message ?: "Face Detection Failed")
    }


    private fun bindFaceView(currenctFace: Face) {
        binding.faceInfo.apply {
            txtSimilarityValue.text = "Nan%"
            txtDistanceValue.text = "0.00"
            txtRealProbabilityValue.text = "0.00"
            txtSmilingValue.text =
                ((currenctFace.smilingProbability ?: 0).toFloat() * 100).toInt().toString() + " %"
            txtLeftEyeValue.text =
                ((currenctFace.leftEyeOpenProbability ?: 0).toFloat() * 100).toInt()
                    .toString() + " %"
            txtRightEyeValue.text =
                ((currenctFace.rightEyeOpenProbability ?: 0).toFloat() * 100).toInt()
                    .toString() + " %"
            txtHeadRotationXValue.text = String.format("%.2f", currenctFace.headEulerAngleX)
            txtHeadRotationYValue.text = String.format("%.2f", currenctFace.headEulerAngleY)
            txtHeadRotationZValue.text = String.format("%.2f", currenctFace.headEulerAngleZ)
        }
    }

    private fun unBindFaceView() {
        binding.faceInfo.apply {
            txtSimilarityValue.text = "Nan%"
            txtDistanceValue.text = "0.00"
            txtRealProbabilityValue.text = "0.00"
            txtSmilingValue.text = "0.00"
            txtLeftEyeValue.text = "0.00"
            txtRightEyeValue.text = "0.00"
            txtHeadRotationXValue.text = "0.00"
            txtHeadRotationYValue.text = "0.00"
            txtHeadRotationZValue.text = "0.00"
        }
    }

    companion object {
        private val TAG = FaceDetectionActivity::class.simpleName
    }


}