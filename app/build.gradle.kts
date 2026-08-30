plugins {
    alias(libs.plugins.android.application)
    // ADD THIS LINE:
    id("com.google.gms.google-services")
}

android {
    namespace = "com.karmcharikatta.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.karmcharikatta.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    // Import the Firebase BoM (manages compatible versions automatically)
    implementation(platform("com.google.firebase:firebase-bom:34.18.0"))

    // Firebase Firestore (for storing and fetching GR details/lists)
    implementation("com.google.firebase:firebase-firestore")

    // Firebase Storage (for downloading/reading PDF files)
    implementation("com.google.firebase:firebase-storage")

    // Firebase Authentication (for admin privileges)
    implementation("com.google.firebase:firebase-auth")
}