import com.android.build.api.dsl.ApplicationExtension
import com.kabi.convention.ExtensionType
import com.kabi.convention.configureBuildTypes
import com.kabi.convention.configureKotlinAndroid
import com.kabi.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.run {
            pluginManager.run {
                apply("com.android.application")
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    applicationId = libs.findVersion("projectApplicationId").get().toString()
                    targetSdk = libs.findVersion("projectTargetSdkVersion").get().toString().toInt()

                    versionName = libs.findVersion("projectVersionName").get().toString()
                    versionCode = libs.findVersion("projectVersionCode").get().toString().toInt()
                }

                configureKotlinAndroid(this)

                configureBuildTypes(
                    commonExtension = this,
                    extensionType = ExtensionType.APPLICATION
                )

                /*buildFeatures {
                    buildConfig = true
                }*/
            }

        }
    }
}
