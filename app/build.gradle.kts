import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

val releaseProperties = Properties()
val releasePropertiesFile = rootProject.file("release.properties")
if (releasePropertiesFile.exists()) {
    releaseProperties.load(FileInputStream(releasePropertiesFile))
}

android {
    namespace = "com.japygo.modakmodak"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.japygo.modakmodak"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            
            buildConfigField("String", "ADMOB_BANNER_ID", "\"${releaseProperties.getProperty("ADMOB_BANNER_ID") ?: ""}\"")
            buildConfigField("String", "ADMOB_REWARDED_FOCUS_ID", "\"${releaseProperties.getProperty("ADMOB_REWARDED_FOCUS_ID") ?: ""}\"")
            buildConfigField("String", "ADMOB_REWARDED_SHOP_ID", "\"${releaseProperties.getProperty("ADMOB_REWARDED_SHOP_ID") ?: ""}\"")
        }
        debug {
            applicationIdSuffix = ".debug"
            
            buildConfigField("String", "ADMOB_BANNER_ID", "\"${localProperties.getProperty("ADMOB_BANNER_ID") ?: "ca-app-pub-3940256099942544/6300978111"}\"")
            buildConfigField("String", "ADMOB_REWARDED_FOCUS_ID", "\"${localProperties.getProperty("ADMOB_REWARDED_FOCUS_ID") ?: "ca-app-pub-3940256099942544/5224354917"}\"")
            buildConfigField("String", "ADMOB_REWARDED_SHOP_ID", "\"${localProperties.getProperty("ADMOB_REWARDED_SHOP_ID") ?: "ca-app-pub-3940256099942544/5224354917"}\"")
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.lottie.compose)
    implementation(libs.play.services.ads)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}