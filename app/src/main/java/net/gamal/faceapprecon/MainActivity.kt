package net.gamal.faceapprecon

import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.Python
import net.gamal.faceapprecon.databinding.ActivityMainBinding
import net.gamal.faceapprecon.ml.TFLiteModelExecutor

class MainActivity : AppCompatActivity() {

    private val cameraPermission = android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonOpenScanner.setOnClickListener {
//            runPythonFile()
            // Sample input data for a hypothetical image classification model
            val imageWidth = 224
            val imageHeight = 224
            val numChannels = 3

// Generate random pixel values between 0 and 1 for each channel
            val inputData = FloatArray(imageWidth * imageHeight * numChannels) {
                Math.random().toFloat()  // Replace this with your actual image data
            }

// Ensure inputData is not empty
            if (inputData.isNotEmpty()) {
                TFLiteModelExecutor.executeTensorModel(lifecycleScope, this, inputData)
            } else {
                // Handle the case when inputData is empty
            }
        }


        binding.buttonFaceDetect.setOnClickListener {
            requestCameraAndStart()
        }
    }


    private fun runPythonFile() {
        val py = Python.getInstance()
        val module = py.getModule("logic_operations")
        val mainFun = module["main"]
        val result = mainFun?.call("1", "False", "True")
        Log.e("mainFun", "runPythonFile: result=$result")
    }

    private fun requestCameraAndStart() {
        if (isPermissionGranted(cameraPermission)) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun startCamera() {
        navToActivity(FaceDetectionActivity::class.java)
    }

    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest(
                    positive = { openPermissionSetting() }
                )
            }

            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}