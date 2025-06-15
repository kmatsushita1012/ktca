repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "2.0.21"
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
