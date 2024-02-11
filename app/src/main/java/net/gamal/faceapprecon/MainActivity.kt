package net.gamal.faceapprecon

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import net.gamal.faceapprecon.databinding.ActivityMainBinding
import net.gamal.faceapprecon.utilities.cameraPermissionRequest
import net.gamal.faceapprecon.utilities.isPermissionGranted
import net.gamal.faceapprecon.utilities.openPermissionSetting


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val cameraPermission = android.Manifest.permission.CAMERA
    private lateinit var binding: ActivityMainBinding
    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted ->

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestCameraAndStart()
        val w = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        val navController = this.findNavController(R.id.navHostFragment)
        binding.bottomNav.setupWithNavController(navController)
    }

    private fun requestCameraAndStart() {
        if (isPermissionGranted(cameraPermission)) {
        } else {
            requestCameraPermission()
        }
    }


    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest(positive = { openPermissionSetting() })
            }

            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }
}