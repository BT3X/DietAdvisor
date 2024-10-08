plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    kotlin("kapt")
}

android {
    namespace = "com.kkt.dietadvisor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kkt.dietadvisor"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["appAuthRedirectScheme"] = "com.googleusercontent.apps.908769963821-8hcd506sl9obet9u6ki61mahs9bclj5i"
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
        release {
            isDebuggable = false
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "35.0.0"
}

dependencies {

    // For type-safe navigation (Currently in beta)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Material Icons Extended
    implementation(libs.androidx.material.icons.extended)

    // Constraint Layout
    implementation(libs.androidx.constraintlayout.compose)

    // Pie Chart XML
    implementation(libs.pie.chart)

    // XML Constraint Layout
    implementation(libs.androidx.constraintlayout)

    // Material Library
    implementation(libs.material)

    // Credentials API
    implementation(libs.androidx.credentials)
    // optional - needed for credentials support from play services, for devices running
    // Android 13 and below.
    implementation(libs.androidx.credentials.play.services.auth)

    // Auth API
    implementation (libs.play.services.auth)
    implementation (libs.androidx.activity.ktx)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Bar Chart
    implementation(libs.mpandroidchart)

    // OKHttp
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)

    // GSON Lib
    implementation (libs.gson)

    // Glide Image Loader
    implementation (libs.glide)

    // AppAuth Library
    implementation (libs.appauth)

    // Encrypted Shared Prefs
    implementation (libs.androidx.security.crypto)

    // Markwon Markdown Renderer
    kapt(libs.prism4j.bundler)
    implementation(libs.core)
    implementation (libs.image)
    implementation (libs.recycler)
    implementation("io.noties.markwon:syntax-highlight:4.6.2") {
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}