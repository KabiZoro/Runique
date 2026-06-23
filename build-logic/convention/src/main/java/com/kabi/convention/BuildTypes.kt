package com.kabi.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.BuildType
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.DynamicFeatureExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.Project

internal fun Project.configureBuildTypes(
    commonExtension: CommonExtension,
    extensionType: ExtensionType
) {

    commonExtension.run {

        val apiKey =
            gradleLocalProperties(rootDir, rootProject.providers)
                .getProperty("API_KEY") ?: ""

        buildFeatures.buildConfig = true

        when (extensionType) {
            ExtensionType.APPLICATION -> {
                (this as ApplicationExtension).buildTypes {
                    debug {
                        configureDebugBuildType(apiKey)
                    }
                    release {
                        configureReleaseBuildType(commonExtension, apiKey)
                    }
                }
            }

            ExtensionType.LIBRARY -> {
                (this as LibraryExtension).buildTypes {
                    debug {
                        configureDebugBuildType(apiKey)
                    }
                    release {
                        configureReleaseBuildType(commonExtension, apiKey)
                    }
                }
            }

            ExtensionType.DYNAMIC_FEATURE -> {
                (this as DynamicFeatureExtension).buildTypes {
                    debug {
                        configureDebugBuildType(apiKey)
                    }
                    release {
                        configureReleaseBuildType(commonExtension, apiKey)
                        isMinifyEnabled = false
                    }
                }
            }
        }
    }
}

private fun BuildType.configureDebugBuildType(apiKey: String) {
    buildConfigField("String", "API_KEY", "\"$apiKey\"")
    buildConfigField("String", "BASE_URL", "\"https://runique.pl-coding.com:8080\"")
//    buildConfigField("String", "BASE_URL", "\"http://192.168.0.102:8080\"")
}

private fun BuildType.configureReleaseBuildType(
    commonExtension: CommonExtension,
    apiKey: String
) {
    buildConfigField("String", "API_KEY", "\"$apiKey\"")
    buildConfigField("String", "BASE_URL", "\"https://runique.pl-coding.com:8080\"")
//    buildConfigField("String", "BASE_URL", "\"http://192.168.0.102:8080\"")

    isMinifyEnabled = true
    proguardFiles(
        commonExtension.getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}