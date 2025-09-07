plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.rebound"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rebound"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.tbuonomo:dotsindicator:4.2")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("com.sun.mail:android-mail:1.6.7")
    implementation ("com.sun.mail:android-activation:1.6.7")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.zxing:core:3.5.2")
    implementation("com.google.firebase:firebase-auth:23.2.1")
    implementation("com.google.android.material:material:1.11.0")

    // CameraX
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.camera.extensions)
    implementation(libs.camera.video)

    // Navigation
    implementation(libs.navigation.fragmentKtx)
    implementation(libs.navigation.uiKtx)

    // Fragment
    implementation(libs.fragment.ktx)

    // WindowManager
    implementation(libs.androidx.window)

    // Unit testing
    testImplementation(libs.junit)

    // Instrumented testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // MediaPipe Library
    implementation(libs.mediapipe.tasksVision)
    implementation(libs.mediapipe.tasksCore)

    // Filament dependencies
    implementation(libs.gltfio.android)
    implementation(libs.filament.utils) // Corrected to use direct alias
    implementation(libs.filament.android) // Corrected to use direct alias

}