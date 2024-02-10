plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.chaquo.python")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "net.gamal.faceapprecon"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.gamal.faceapprecon"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        mlModelBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.3.0")
    implementation("com.google.mlkit:vision-common:17.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.activity:activity-ktx:1.8.2")

    val cameraVersion = "1.3.1"
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")


    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.mlkit:face-detection:16.1.5")
    implementation ("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")

    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.40")
    kapt("com.google.dagger:hilt-compiler:2.40")

    // For ViewModel support
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03")
}