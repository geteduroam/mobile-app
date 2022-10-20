package app.eduroam.shared

object Libs {
    const val androidGradlePlugin = "com.android.tools.build:gradle:7.3.0"
    const val googleService = "com.google.gms:google-services:4.3.14"
    const val crashlyticsPlugin = "com.google.firebase:firebase-crashlytics-gradle:2.8.1"
    const val firebaseDistributionPlugin =
        "com.google.firebase:firebase-appdistribution-gradle:3.0.1"
    const val timberLog = "com.jakewharton.timber:timber:5.0.1"
    const val desugaring = "com.android.tools:desugar_jdk_libs:1.1.5"
    const val scribejava = "com.github.scribejava:scribejava-core:8.3.1"

    object AndroidX {
        const val core = "androidx.core:core-ktx:1.9.0"
        const val appcompat = "androidx.appcompat:appcompat:1.6.0-rc01"
        const val splashCore = "androidx.core:core-splashscreen:1.0.0"

        object Activity {
            const val activityCompose = "androidx.activity:activity-compose:1.6.0"
        }

        object Compose {
            const val snapshot = ""
            private const val version = "1.3.0-beta03"
            private const val kotlinCompilerExtensionVersion = "1.3.1"

            const val runtime = "androidx.compose.runtime:runtime:$version"
            const val compiler =
                "androidx.compose.compiler:compiler:$kotlinCompilerExtensionVersion"
            const val runtimeLivedata = "androidx.compose.runtime:runtime-livedata:$version"
            const val material = "androidx.compose.material:material:$version"
            const val materialIconsExt =
                "androidx.compose.material:material-icons-extended:$version"
            const val foundation = "androidx.compose.foundation:foundation:$version"
            const val layout = "androidx.compose.foundation:foundation-layout:$version"
            const val tooling = "androidx.compose.ui:ui-tooling:$version"
            const val toolingPreview = "androidx.compose.ui:ui-tooling-preview:$version"
            const val animation = "androidx.compose.animation:animation:$version"
            const val uiTest = "androidx.compose.ui:ui-test-junit4:$version"
            const val uiTestManifest = "androidx.compose.ui:ui-test-manifest:$version"

            object Material3 {
                private const val version = "1.0.0-alpha09"
                const val designMaterial3 = "androidx.compose.material3:material3:$version"
            }

            object Navigation {
                private const val version = "2.6.0-alpha01"
                const val nav = "androidx.navigation:navigation-compose:$version"
            }
        }

        object Lifecycle {
            private const val version = "2.6.0-alpha02"
            const val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:$version"
            const val viewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            const val runtimeCompose = "androidx.lifecycle:lifecycle-runtime-compose:$version"
        }

        object Test {
            private const val version = "1.4.0"
            const val core = "androidx.test:core:$version"
            const val runner = "androidx.test:runner:$version"
            const val rules = "androidx.test:rules:$version"

            object Ext {
                private const val version = "1.1.2"
                const val junit = "androidx.test.ext:junit-ktx:$version"
            }

            const val espressoCore = "androidx.test.espresso:espresso-core:3.2.0"
        }
    }

    object Accompanist {
        private const val version = "0.26.5-rc"

        const val pager = "com.google.accompanist:accompanist-pager:$version"
        const val pagerIndicators = "com.google.accompanist:accompanist-pager-indicators:$version"
        const val permissions = "com.google.accompanist:accompanist-permissions:$version"
        const val webview = "com.google.accompanist:accompanist-webview:$version"
    }

    object Coil {
        private const val version = "2.2.1"
        const val coilCompose = "io.coil-kt:coil-compose:$version"
    }

    object DateTime {
        private const val version = "0.4.0"
        const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:$version"
    }

    object Firebase {
        private const val version = "30.5.0"
        const val billOfMaterials = "com.google.firebase:firebase-bom:$version"

        //When using BOM do not specify the versions in Firebase library dependencies
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
    }

    object Hilt {
        private const val version = "2.39"

        const val gradlePlugin = "com.google.dagger:hilt-android-gradle-plugin:$version"
        const val android = "com.google.dagger:hilt-android:$version"
        const val compiler = "com.google.dagger:hilt-compiler:$version"
        const val testing = "com.google.dagger:hilt-android-testing:$version"
    }

    object Testing {
        const val junit = "junit:junit:4.13.2"
        const val kotlinTest = "org.jetbrains.kotlin:kotlin-test"
        const val test = "org.jetbrains.kotlin:test"
    }

    object Koin {
        private const val version = "3.2.2"

        const val core = "io.insert-koin:koin-core:$version"
        const val android = "io.insert-koin:koin-android:$version"
    }

    object Kotlin {
        private const val version = "1.7.10"

        const val stdlibJdk = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        const val stdlibCommon = "org.jetbrains.kotlin:kotlin-stdlib-common:$version"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$version"
        const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val serialization = "org.jetbrains.kotlin:kotlin-serialization:$version"
        const val serializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2"
        const val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"

        object Coroutines {
            private const val version = "1.6.4"
            const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
            const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
        }
    }

    object Ktor {
        private const val version = "2.1.2"
        const val clientCore = "io.ktor:ktor-client-core:$version"
        const val clientJson = "io.ktor:ktor-client-json:$version"
        const val clientSerialization = "io.ktor:ktor-serialization-kotlinx-json:$version"
        const val clientSerializationJvm = "io.ktor:ktor-client-serialization-jvm:$version"
        const val contentNegotiation = "io.ktor:ktor-client-content-negotiation:$version"
        const val clientAndroid = "io.ktor:ktor-client-okhttp:$version"
        const val clientiOS = "io.ktor:ktor-client-ios:$version"
        const val logging = "io.ktor:ktor-client-logging:$version"
    }

    object Material {
        private const val version = "1.8.0-alpha01"
        const val design = "com.google.android.material:material:$version"
    }

    object SqlDelight {
        const val version = "1.5.3"
        const val androidDriver = "com.squareup.sqldelight:android-driver:$version"
        const val iOsDriver = "com.squareup.sqldelight:native-driver:$version"
        const val gradlePlugin = "com.squareup.sqldelight:gradle-plugin:$version"
    }

    object Touchlab {
        const val kermit = "co.touchlab:kermit:1.2.1-alpha"
        const val stately = "co.touchlab:stately-common:1.2.3"
    }
}

object Urls {
    const val mavenCentralSnapshotRepo = "https://oss.sonatype.org/content/repositories/snapshots/"
    const val composeSnapshotRepo = "https://androidx.dev/snapshots/builds/" +
            "${Libs.AndroidX.Compose.snapshot}/artifacts/repository/"
}
