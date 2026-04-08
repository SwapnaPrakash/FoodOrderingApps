plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
}

android {
    namespace = "com.swapna.foodapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.swapna.foodapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String", "BASE_URL",
            "\"https://raw.githubusercontent.com/SwapnaPrakash/zomato-mock-api/main/\""
           // "\"https://github.com/SwapnaPrakash/zomato-mock-api/blob/main/\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

koverReport {
    filters {
        excludes {
            classes(
                // Hilt generated
                "*.*_HiltModules*",
                "*.*Hilt_*",
                "*.*_Factory*",
                "*.*_MembersInjector*",

                // BuildConfig
                "*.BuildConfig",

                // Room generated
                "*.*_Impl*",

                // Compose generated
                "*.*ComposableSingletons*",

                // DTOs (tested via mapper)
                "*.data.remote.dto.*",
            )
        }
        includes {
            packages(
                "com.swapna.foodapp.presentation",
                "com.swapna.foodapp.domain",
            )
        }
    }
    verify {
        rule {
            bound {
                minValue = 10
            }
        }
    }
}

tasks.named("koverHtmlReport") {
    dependsOn("testDebugUnitTest")
}

dependencies {
    //Compose
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.material.icons.extended)

    // hilt
    implementation(libs.hilt.android)

    //DataStore
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.datastore.preferences)

    //Coil
    implementation(libs.accompanist.permissions)
    implementation(libs.bundles.coil)

    // Image Loading
    implementation(libs.coil.kt.coil.compose)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.moshi.v290)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.interceptor.logging)

    // Moshi
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.moshi.kotlin)

    // OkHttp logging
    implementation(libs.okhttp.interceptor.logging)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    //Room
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    //Pagination
    implementation(libs.androidx.paging.compose)

    // Testing
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

    // Hilt Testing
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    ksp(libs.hilt.android.compiler)

    // Logging
    implementation(libs.timber)

    // Kotest JUnit5 engine setup
    tasks.withType<Test> { useJUnitPlatform() }

    //UI test
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    testImplementation(kotlin("test"))

    //Local unit test
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation (libs.mockito.kotlin)

    // Kotest
    testImplementation (libs.io.kotest.kotest.runner.junit52)
    testImplementation (libs.kotest.assertions.core)
    testImplementation (libs.kotest.framework.engine)
    testImplementation (libs.mockk)
    testImplementation (libs.kotlinx.coroutines.test.v173)
    testImplementation("androidx.test:core:1.5.0")

}
