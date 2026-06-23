plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.runique.android.application.compose)
    alias(libs.plugins.runique.jvm.ktor)
}

android {
    namespace = "com.kabi.runique"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    dynamicFeatures += setOf(":analytics:analytics_feature")
}

dependencies {

    implementation(projects.auth.data)
    implementation(projects.auth.domain)
    implementation(projects.auth.presentation)

    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.domain)
    implementation(projects.core.presentation.designsystem)
    implementation(projects.core.presentation.ui)

    implementation(projects.run.data)
    implementation(projects.run.domain)
    implementation(projects.run.location)
    implementation(projects.run.network)
    implementation(projects.run.presentation)

    implementation(projects.core.connectivity.domain)
    implementation(projects.core.connectivity.data)
    implementation(projects.core.notification)

    // Coil
    implementation(libs.coil.compose)

    // Compose
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // Crypto
    implementation(libs.androidx.security.crypto.ktx)

    // Koin
    implementation(libs.bundles.koin)

    api(libs.core)

    // Location
    implementation(libs.google.android.gms.play.services.location)

    // Splash screen
    implementation(libs.androidx.core.splashscreen)

    // Timber
    implementation(libs.timber)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}