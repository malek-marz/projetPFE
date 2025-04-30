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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Ajout de la clé API dans les buildConfigFields
        buildConfigField("String", "API_KEY", "\"AIzaSyDlQiusEchlTg8jq6_SrW1nWG2-epihOj8\"")
    }

    buildFeatures {
        buildConfig = true // Assurez-vous que la génération de BuildConfig est activée
        compose = true
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
}

dependencies {
    // Compose dependencies
    implementation("androidx.compose.foundation:foundation:1.5.0")
    implementation(libs.androidx.material)
    implementation("com.google.accompanist:accompanist-flowlayout:0.30.1")
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation("androidx.compose.foundation:foundation:1.4.3")
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.espresso.core)
    implementation(libs.support.annotations)
    implementation(libs.mediation.test.suite)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit dependencies for API calls
    implementation(libs.retrofit)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("io.coil-kt:coil-compose:2.2.0")

    // Glide for image loading (version compatible KSP)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    ksp("com.github.bumptech.glide:ksp:4.15.1")

    // LiveData and ViewModel dependencies
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation(libs.converter.gson)

    // Accompanist for pager and flow layout
    implementation("com.google.accompanist:accompanist-pager:0.30.1")
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation component for Compose
    implementation(libs.compose.navigation)

    // Room dependencies for local database
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // AI dependencies
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
}
