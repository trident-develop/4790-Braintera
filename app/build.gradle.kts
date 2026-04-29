import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
    id("org.lsposed.lsparanoid")
}

lsparanoid {
    seed = 47671282
    classFilter = { it.startsWith("com.microdose.ball") }
    includeDependencies = true
    variantFilter = { true }
}

android {
    namespace = "com.microdose.ball"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.microdose.ball"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.core.splashscreen)
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
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.ads)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.installreferrer)
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("androidx.fragment:fragment:1.8.9")
    implementation("com.facebook.android:facebook-android-sdk:18.0.3")
}

afterEvaluate {
    tasks.named("uploadCrashlyticsMappingFileRelease")
        .configure { enabled = false }
}

afterEvaluate {
    tasks.named("bundleRelease").configure {
        finalizedBy("removeProguardMap")
    }
}

tasks.register("removeProguardMap") {
    doLast {
        val generatedAabPath = "${projectDir}/release"
        val aabFile = file("${generatedAabPath}/app-release.aab")

        val zipFile = file("${generatedAabPath}/app-release.zip")
        val savedProguardMapFile = file("${generatedAabPath}/proguard.map")
        val tempZipFilePath = file("${generatedAabPath}/app-release-temp.zip")
        val targetFilePath = "BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map"

        aabFile.renameTo(zipFile)

        val zf = ZipFile(zipFile)
        val zos = ZipOutputStream(tempZipFilePath.outputStream())
        try {
            val entries = zf.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                if (entry.name != targetFilePath) {
                    zos.putNextEntry(ZipEntry(entry.name))
                    zf.getInputStream(entry).use { it.copyTo(zos) }
                    zos.closeEntry()
                } else {
                    zf.getInputStream(entry).use { input ->
                        savedProguardMapFile.outputStream().use { input.copyTo(it) }
                    }
                }
            }
        } finally {
            zos.close()
            zf.close()
        }

        zipFile.delete()
        tempZipFilePath.renameTo(aabFile)
    }
}
