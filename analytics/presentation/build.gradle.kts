plugins {
    alias(libs.plugins.runique.android.feature.ui)
}

android {
    namespace = "com.kabi.analytics.presentation"
}

dependencies {
    implementation(projects.analytics.domain)
}