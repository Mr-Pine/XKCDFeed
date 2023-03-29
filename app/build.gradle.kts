import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.android.gms.oss-licenses-plugin")
}

val signingFile = project.file("signing.properties")
val signingProperties = Properties()
signingProperties.load(FileInputStream(signingFile))

android {
    signingConfigs {
        create("release") {
            storeFile = file(signingProperties["signing.release.storeFile"] as String)
            keyAlias = signingProperties["signing.release.keyAlias"] as String
            storePassword = signingProperties["signing.release.password"] as String
            keyPassword = signingProperties["signing.release.password"] as String
        }
    }
    compileSdk = 33

    defaultConfig {
        applicationId = "de.mr_pine.xkcdfeed"
        minSdk = 22
        targetSdk = 33
        versionCode = 4
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //signingConfig = signingConfigs.release
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
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.accompanist)

    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    implementation(libs.playAuth)

    implementation(libs.ossLicences)
    implementation(libs.volley)
    implementation(libs.coil)
    implementation(libs.zoomables)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${libs.versions.compose.get()}")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xjvm-default=all",
    )
}