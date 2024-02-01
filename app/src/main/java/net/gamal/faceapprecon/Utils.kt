package net.gamal.faceapprecon

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat


fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

inline fun Context.cameraPermissionRequest(crossinline positive: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle("Camera Permission Required")
        .setMessage("Without accessing the camera it is not possible to SCAN QR Codes...")
        .setPositiveButton("Allow Camera") { dialogf                                                                                                                                                                                                                                , which ->
            positive.invoke()
        }.setNegativeButton("Cancel") { dialog, which ->

        }.show()
}

fun Context.openPermissionSetting() {
    Intent(ACTION_APPLICATION_DETAILS_SETTINGS).also {
        val uri: Uri = Uri.fromParts("package", packageName, null)
        it.data = uri
        startActivity(it)
    }
}

fun Activity.navToActivity(
    destinationActivity: Class<out ComponentActivity>, bundle: Bundle? = null,
    clearStacks: Boolean = false
) {
    val intent = Intent(this, destinationActivity)
    bundle?.let { intent.putExtras(it) }
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
    if (clearStacks) finishAffinity()
}