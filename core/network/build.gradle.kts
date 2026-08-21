import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val cmcApiKey: String = localProperties.getProperty("CMC_API_KEY")
    ?: System.getenv("CMC_API_KEY")
    ?: ""

android {
    namespace = "com.aorrico.mymbchallenge.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "CMC_API_KEY", "\"$cmcApiKey\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.moshi.core)
    ksp(libs.moshi.codegen)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.core)

    // library-no-op mirrors the real Chucker API with empty implementations, so the interceptor
    // is wired unconditionally in NetworkModule - no BuildConfig.DEBUG branching needed there.
    // Release builds never pull in Chucker's UI/database code at all, not just disable it.
    debugImplementation(libs.chucker.library)
    releaseImplementation(libs.chucker.library.noop)

    testImplementation(libs.junit4)
    testImplementation(libs.truth)
}
