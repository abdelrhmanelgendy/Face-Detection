package net.gamal.faceapprecon

import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.chaquo.python.android.PyApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApp : PyApplication() {
    override fun onCreate() {
        super.onCreate()
        // "context" must be an Activity, Service or Application object from your app.
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
    }
}