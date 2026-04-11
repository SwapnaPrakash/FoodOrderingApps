plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
}

android {
    namespace  = "com.swapna.foodapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.swapna.foodapp"
        minSdk        = 24
        targetSdk     = 36
        versionCode   = 1
        versionName   = "1.0"

        buildConfigField(
            "String",
            "BASE_URL",
            "\"https://raw.githubusercontent.com/SwapnaPrakash/zomato-mock-api/main/\""
        )

        // ✅ Standard runner — NOT HiltTestRunner
        testInstrumentationRunner = "com.swapna.foodapp.HiltTestRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

// ── Kover Coverage ────────────────────────────────────────────
koverReport {
    filters {
        excludes {
            classes(
                "*.*_HiltModules*",
                "*.*Hilt_*",
                "*.*_Factory*",
                "*.*_MembersInjector*",
                "*.BuildConfig",
                "*.*_Impl*",
                "*.*ComposableSingletons*",
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
            bound { minValue = 10 }
        }
    }
}

tasks.named("koverHtmlReport") {
    dependsOn("testDebugUnitTest")
}

dependencies {

    // ── Compose ───────────────────────────────────────────────
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

    // ── Hilt ──────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // ── DataStore ─────────────────────────────────────────────
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.androidx.datastore.preferences)

    // ── Coil ──────────────────────────────────────────────────
    implementation(libs.accompanist.permissions)
    implementation(libs.bundles.coil)
    implementation(libs.coil.kt.coil.compose)

    // ── Retrofit + OkHttp ─────────────────────────────────────
    implementation(libs.retrofit)
    implementation(libs.converter.moshi.v290)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    // ✅ DEDUPLICATED: was listed twice — kept once
    implementation(libs.okhttp.interceptor.logging)

    // ── Moshi ─────────────────────────────────────────────────
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // ── Coroutines ────────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ── Firebase ──────────────────────────────────────────────
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // ── Room ──────────────────────────────────────────────────
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // ── Pagination ────────────────────────────────────────────
    implementation(libs.androidx.paging.compose)

    // ── Logging ───────────────────────────────────────────────
    implementation(libs.timber)

    // ══════════════════════════════════════════════════════════
    // UNIT TESTS (JVM — no device needed)
    // ══════════════════════════════════════════════════════════
    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    // ✅ DEDUPLICATED: mockito.kotlin was listed twice — kept once
    testImplementation(libs.mockito.kotlin)
    testImplementation(kotlin("test"))
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    // Kotest
    testImplementation(libs.io.kotest.kotest.runner.junit52)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.framework.engine)
    testImplementation(libs.mockk)
    // ✅ DEDUPLICATED: kept only v173 (more specific version)
    testImplementation(libs.kotlinx.coroutines.test.v173)

    // ══════════════════════════════════════════════════════════
    // UI / INSTRUMENTED TESTS (needs device/emulator)
    // ✅ NO Hilt androidTest deps — they crash test APK
    // ✅ NO kspAndroidTest — not needed without Hilt testing
    // ══════════════════════════════════════════════════════════
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.activity:activity-compose:1.9.0")
    androidTestImplementation("androidx.navigation:navigation-compose:2.7.7")

    // ── Debug ─────────────────────────────────────────────────
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

    // hilt
    implementation(libs.hilt.android)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)


}