plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.astromg01.mccompanion"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.astromg01.mccompanion.preview"
        minSdk = 26
        targetSdk = 33
        versionCode = 2
        versionName = "0.2.0-preview"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
