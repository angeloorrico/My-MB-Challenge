plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.aorrico.mymbchallenge.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
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
    implementation(project(":core:network"))
    implementation(project(":domain"))

    implementation(libs.retrofit.core)
    implementation(libs.moshi.core)
    ksp(libs.moshi.codegen)
    kspTest(libs.moshi.codegen)

    implementation(libs.paging.runtime)
    implementation(libs.paging.common)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit4)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.paging.testing)
    testImplementation(libs.retrofit.converter.moshi)
    testImplementation(libs.okhttp.mockwebserver)
}
