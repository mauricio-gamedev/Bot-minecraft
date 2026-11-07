plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.astromg01.mccompanion"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.astromg01.mccompanion"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "0.1.0"
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
