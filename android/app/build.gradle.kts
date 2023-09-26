import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("com.google.protobuf") version (libs.versions.protobufPlugin)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
    kotlin("kapt")
}

if (JavaVersion.current() < JavaVersion.VERSION_17) {
    throw GradleException("Please use JDK ${JavaVersion.VERSION_17} or above")
}

val buildNumberFile = File("${buildDir}/build_number.txt")
val versionNameFile = File("${projectDir}/version_name.txt")
val appCode = if (buildNumberFile.exists()) {
    buildNumberFile.readText().trim().toInt()
} else {
    1
}
val appName = if (versionNameFile.exists()) {
    versionNameFile.readText() + "($appCode)"
} else {
    "Alpha($appCode)"
}

android {
    compileSdkPreview = "UpsideDownCake"
    namespace = "app.eduroam.geteduroam"

    defaultConfig {
        applicationId = "app.eduroam.geteduroam"
        minSdk = libs.versions.android.sdk.min.get().toInt()
        targetSdk = libs.versions.android.sdk.target.get().toInt()
        versionCode = appCode
        versionName = appName
        manifestPlaceholders += mapOf(
            "appAuthRedirectScheme" to "app.eduroam.geteduroam"
        )
        firebaseAppDistribution {
            artifactType = "APK"
            releaseNotesFile = "${buildDir}/release_notes.txt"
            groups = "All testers"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.android.material)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.splash.core)
    implementation(libs.timber)

    implementation(libs.androidx.datastore)
    implementation(libs.google.protobuf)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.bottomsheet)
    implementation(libs.appauth)
    implementation(libs.simpleframework.xml.parser)

    //Moshi dependency for parsing with ksp i.s.o. kapt
    implementation(libs.moshi.moshi)
    ksp(libs.moshi.codegen)

    //OkHttp client
    implementation(libs.okhttp.okhttp)
    implementation(libs.okhttp.logging)

    //Retrofit with moshi for API calls
    implementation(libs.retrofit.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.retrofit.converter.scalars)

    //Compose BOM dependencies
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.tool.preview)
    implementation(libs.androidx.compose.lifecycle.vm)

    //Compose navigation and hiltViewModel
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.hilt.navigation)

    //Hilt dependencies
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    //Compose debug/tool preview
    debugImplementation(libs.androidx.compose.debug.ui.tooling)
    debugImplementation(libs.androidx.compose.debug.ui.test.manifest)
}