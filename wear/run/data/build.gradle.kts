plugins {
    alias(libs.plugins.runique.android.library)
}

android {
    namespace = "com.kabi.wear.run.data"

    defaultConfig {
        minSdk = 30
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.wear.run.domain)
    implementation(projects.core.connectivity.domain)

    implementation(libs.androidx.health.services.client)
    implementation(libs.bundles.koin)
}