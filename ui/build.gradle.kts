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

        // androidxのテストランナーに変更
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.22"
    }
}

dependencies {
    // Compose BOMを使ってCompose関連のバージョン管理を一元化
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.foundation.layout)  // foundation-layoutの正しい依存名

    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.espresso.core)
}

group = "com.github.kmatsushita1012"
version = "1.0"

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.kmatsushita1012"
                artifactId = "ktca-ui"
                version = "1.0"
            }
        }
    }
}
