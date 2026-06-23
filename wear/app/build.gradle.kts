plugins {
    alias(libs.plugins.runique.android.application.wear.compose)
}

android {
    namespace = "com.kabi.wear.app"

    defaultConfig {
        minSdk = 30
    }
    useLibrary("wear-sdk")
}

dependencies {

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.bundles.koin.compose)

    implementation(projects.core.presentation.designsystemWear)
    implementation(projects.wear.run.presentation)
    implementation(projects.wear.run.data)

    implementation(projects.core.connectivity.domain)
    implementation(projects.core.connectivity.data)
    implementation(projects.core.notification)

    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.compose.foundation)
//    implementation(libs.androidx.wear.tooling.preview)
//    implementation(libs.compose.material3)
//    implementation(libs.compose.ui.tooling)
    implementation(libs.play.services.wearable)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}