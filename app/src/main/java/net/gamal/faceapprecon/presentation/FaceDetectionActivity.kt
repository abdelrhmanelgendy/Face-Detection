package net.gamal.faceapprecon.presentation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.gamal.faceapprecon.R
import net.gamal.faceapprecon.camera.data.model.FaceBox
import net.gamal.faceapprecon.databinding.ActivityFaceDetectionBinding
import net.gamal.faceapprecon.presentation.dialogs.SaveFaceDialog
import net.gamal.faceapprecon.presentation.mvi.CameraXViewModel
import net.gamal.faceapprecon.presentation.mvi.FaceDetectionContract
import net.gamal.faceapprecon.presentation.mvi.FaceDetectionViewModel
import net.gamal.faceapprecon.utilities.hapticFeeds.NFCHapticFeeds
import net.gamal.faceapprecon.utilities.utils.ImageDetectorUtil
import net.gamal.faceapprecon.utilities.utils.MediaUtils.flip

@AndroidEntryPoint
class FaceDetectionActivity : AppCompatActivity() {

    private var currentBox: Bitmap? = null
    private lateinit var binding: ActivityFaceDetectionBinding
    private val cameraXViewModel by viewModels<CameraXViewModel>()
    private val faceDetectionViewModel by viewModels<FaceDetectionViewModel>()
    private var face_detection_paused = false
    private val nfcHapticFeeds = NFCHapticFeeds(this)

    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupCamera()
        setupCameraButtons()
        setupSaveFaceButtons()
        observerOnVMEvent()
        observerOnVMState()
        faceDetectionViewModel.processIntent(FaceDetectionContract.FaceDetectionAction.FetchListOfFaceDetections)
    }

    private fun observerOnVMState() {
        lifecycleScope.launch {
            faceDetectionViewModel.viewState.collectLatest { event ->
                if (event.exception != null) {
                    showSnackBar(event.exception.message!!)
                    saveFaceDialog.dismiss()
                }
            }
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun observerOnVMEvent() {
        lifecycleScope.launch {
            faceDetectionViewModel.singleEvent.collect { event ->
                when (event) {
                    is FaceDetectionContract.FaceDetectionEvent.FetchedListOfFaces -> {
                        println("observerOnVMState:: FetchedListOfFaces:: ${event.faces}")
                    }

                    is FaceDetectionContract.FaceDetectionEvent.FetchedFaceByID -> {
                        println("observerOnVMState::  FetchedFaceByID:: ${event.face}")
                    }

                    FaceDetectionContract.FaceDetectionEvent.FaceInsertedSuccessfully -> {
                        saveFaceDialog.dismiss()
                        showSnackBar("Face Inserted Successfully")
                    }

                    is FaceDetectionContract.FaceDetectionEvent.FaceRecognizedSuccessfully -> {
                        println("observerOnVMState::  FaceRecognizedSuccessfully:: ${event.recognizedFace}")
                        binding.faceInfo.root.visibility = android.view.View.INVISIBLE
                        binding.faceFoundView.root.visibility = android.view.View.VISIBLE
                        binding.faceFoundView.txtFaceName.text = StringBuilder().apply {
                            append("1 Match Found With\n")
                            append("Name: ")
                            append(event.recognizedFace.name)
                        }
                        binding.faceFoundView.ivSavedImage.setImageBitmap(event.recognizedFace.faceImage)
                        nfcHapticFeeds.playSound(300)

                    }
                }
            }

        }
    }

    private val saveFaceDialog = SaveFaceDialog()

    private fun setupSaveFaceButtons() {
        saveFaceDialog.isCancelable = false
        saveFaceDialog.setOnSaveFaceClicked { name, bitmap ->
            bitmap?.let {
                faceDetectionViewModel.processIntent(
                    FaceDetectionContract.FaceDetectionAction.EncodeAndInsertFace(
                        name, it, lifecycleScope, this
                    )
                )
            }
        }
        saveFaceDialog.setonDismiss {
            face_detection_paused = false
        }

        binding.saveFaceButton.setOnClickListener {
            currentBox?.let {
                saveFaceDialog.setFaceBitmap(it)
                saveFaceDialog.show(supportFragmentManager, "SaveFaceDialog")
                face_detection_paused = true
            }
        }
    }

    @ExperimentalGetImage
    private fun setupCamera() {
        cameraXViewModel.setupCamera(this, binding.cameraPreview, ::onGetImageProxy)
    }

    private fun onGetImageProxy(imageProxy: ImageProxy) {
        faceDetectionViewModel.startDetection(
            imageProxy, ::onSuccess, ::onFailure
        )
    }

    @ExperimentalGetImage
    private fun setupCameraButtons() {
        binding.switchCamera.setOnClickListener {
            cameraXViewModel.switchCamera(
                binding.cameraPreview, this, ::onGetImageProxy
            )
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun onSuccess(faces: List<Face>, imageProxy: ImageProxy?) {
        // Handle the case where no faces are detected
        if (face_detection_paused) {
            return
        }
        binding.graphicOverlay.clear()
        if (faces.isEmpty() || imageProxy == null) {
            unBindFaceView()
//            // ...handle no faces scenario...
            lifecycleScope.launch {
                binding.faceInfo.root.visibility = android.view.View.VISIBLE
                binding.faceFoundView.root.visibility = android.view.View.INVISIBLE
            }
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
        this.currentBox =
            if (cameraXViewModel.getCurrentCamera() == CameraSelector.LENS_FACING_FRONT) {
                cropToBBox?.flip(horizontal = true)?.getOrNull()
            } else {
                cropToBBox
            }
        println("onSuccess:: Face Detected:: ${faces.size}")
        cropToBBox?.let { bitmap ->
            faceDetectionViewModel.processIntent(
                FaceDetectionContract.FaceDetectionAction.EncodeAndFindFace(
                    lifecycleScope, this, bitmap
                )
            )
        }
    }

    private fun onFailure(exception: Exception) {
        Log.e(TAG, exception.message ?: "Face Detection Failed")
    }


    @SuppressLint("SetTextI18n")
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
            ivWork.setImageDrawable(ContextCompat.getDrawable(this@FaceDetectionActivity, R.drawable.baseline_person_24))
        }
    }

    override fun onStop() {
        super.onStop()
        cameraXViewModel.clear()
        currentBox = null
        onSuccess(emptyList(), null)
    }

    companion object {
        private val TAG = FaceDetectionActivity::class.simpleName
    }


}