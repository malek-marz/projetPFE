plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.testapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.testapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_KEY", "\"AIzaSyDlQiusEchlTg8jq6_SrW1nWG2-epihOj8\"")

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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.google.auth.library.oauth2.http)
    implementation(libs.androidx.espresso.core)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.accompanist.pager)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // navigation
    implementation(libs.compose.navigation)
    implementation(libs.accompanist.flowlayout.v0280)

    // room
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    // Kotlin serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation ("io.coil-kt:coil-compose:2.2.2")
}
