plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    `maven-publish`
}

android {
    namespace = "com.studiomk.ktca.ui"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true // Composeを有効にする
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.22"
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.foundation.layout.android)
    implementation(libs.androidx.foundation.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)
}
group = "com.github.kmatsushita1012"
version = "1.0"

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"]) // または "debug" など
                groupId = "com.github.kmatsushita1012"
                artifactId = "ktca-ui"
                version = "1.0"
            }
        }
    }
}