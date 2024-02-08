package net.gamal.faceapprecon

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import net.gamal.faceapprecon.databinding.ActivityMainBinding
import net.gamal.faceapprecon.facedetextion.presentation.FaceDetectionActivity

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

        binding.buttonFaceDetect.setOnClickListener {
            requestCameraAndStart()
        }
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