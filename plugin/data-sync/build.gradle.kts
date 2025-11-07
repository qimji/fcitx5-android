import com.android.build.gradle.tasks.MergeSourceSetFolders

plugins {
    id("org.fcitx.fcitx5.android.app-convention")
    id("org.fcitx.fcitx5.android.plugin-app-convention")
    id("org.fcitx.fcitx5.android.build-metadata")
    id("org.fcitx.fcitx5.android.data-descriptor")
}

android {
    namespace = "org.fcitx.fcitx5.android.plugin.datasync"

    defaultConfig {
        applicationId = "org.fcitx.fcitx5.android.plugin.datasync"
    }

    buildTypes {
        release {
            resValue("string", "app_name", "@string/app_name_release")
        }
        debug {
            resValue("string", "app_name", "@string/app_name_debug")
        }
    }
}

// ensure aboutlibraries metadata is bundled if provided
// (placeholder for future assets, kept for parity with other plugins)
tasks.withType<MergeSourceSetFolders>().configureEach {
    if (name.endsWith("Assets")) {
        doLast {
            // no-op
        }
    }
}

dependencies {
    implementation(project(":lib:plugin-base"))
    implementation(libs.kotlinx.coroutines)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)
}
