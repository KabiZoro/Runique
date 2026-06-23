plugins {
    alias(libs.plugins.runique.android.library)
    alias(libs.plugins.runique.jvm.ktor)
}

android {
    namespace = "com.kabi.core.data"
}

dependencies {
    implementation(libs.bundles.koin)

    implementation(projects.core.database)
    implementation(projects.core.domain)

    implementation(libs.timber)
    implementation(libs.androidx.datastore.preferences)

//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
}