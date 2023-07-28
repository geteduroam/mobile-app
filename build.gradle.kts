// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    //Try to avoid using KAPT, check if the library can work/supports KSP which is much faster.
    alias(libs.plugins.kapt) apply false
    //The plugin to use i.s.o. KAPT: https://developer.android.com/build/migrate-to-ksp
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.gms.gradle) apply false
    alias(libs.plugins.protobuf) apply false
}