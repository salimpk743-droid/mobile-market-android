import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val keystoreProps = Properties()
val keystorePropsFile = rootProject.file("keystore/keystore.properties")

if (keystorePropsFile.exists()) {
    keystorePropsFile.inputStream().use { keystoreProps.load(it) }
}

fun envOrProperty(key: String): String? {
    val envKey = when (key) {
        "storeFile" -> "MM_STORE_FILE"
        "storePassword" -> "MM_STORE_PASSWORD"
        "keyAlias" -> "MM_KEY_ALIAS"
        "keyPassword" -> "MM_KEY_PASSWORD"
        else -> key
    }

    return System.getenv(envKey)?.trim()?.takeIf { it.isNotEmpty() }
        ?: keystoreProps.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }
}

val resolvedStoreFile = envOrProperty("storeFile") ?: "keystore/upload-keystore.jks"
val resolvedStorePassword = envOrProperty("storePassword") ?: ""
val resolvedKeyAlias = envOrProperty("keyAlias") ?: "upload"
val resolvedKeyPassword = envOrProperty("keyPassword") ?: ""

android {
    namespace = "pk.mobilemarket.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "pk.mobilemarket.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        resourceConfigurations += listOf("en", "ur")
    }

    signingConfigs {
        create("release") {
            storeFile = file(resolvedStoreFile)
            storePassword = resolvedStorePassword
            keyAlias = resolvedKeyAlias
            keyPassword = resolvedKeyPassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.androidbrowserhelper:androidbrowserhelper:2.6.1")
    implementation("com.google.android.material:material:1.12.0")
}