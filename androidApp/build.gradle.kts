import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "zhiqiu.car.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    // 签名来源优先级：环境变量（CI: GitHub Actions Secrets）> 本地 signing.properties（仅本地，不进 git）
    // 未配置任何签名时，release 退化为使用 debug 签名，保证流水线不崩。
    val signingPropsFile = rootProject.file("androidApp/signing.properties")
    val signingProps = Properties()
    if (signingPropsFile.exists()) {
        signingPropsFile.inputStream().use { stream ->
            signingProps.load(stream)
        }
    }

    fun prop(name: String, envName: String): String? =
        System.getenv(envName) ?: signingProps.getProperty(name)

    val storeFilePath = prop("storeFile", "KEYSTORE_FILE")
    val storePassword = prop("storePassword", "KEYSTORE_PASSWORD")
    val keyAlias = prop("keyAlias", "KEY_ALIAS")
    val keyPassword = prop("keyPassword", "KEY_PASSWORD")

    signingConfigs {
        create("release") {
            if (storeFilePath != null && storePassword != null && keyAlias != null && keyPassword != null) {
                storeFile = file(storeFilePath)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            } else {
                // 未配置签名信息：复用 debug 签名（CI 未设 Secrets 时也能跑通）
                initWith(getByName("debug"))
            }
        }
    }

    defaultConfig {
        applicationId = "zhiqiu.car.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a")
            isUniversalApk = false
        }
    }
}