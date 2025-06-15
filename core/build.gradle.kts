repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "2.0.21"
    `maven-publish`
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
group = "com.github.kmatsushita1012"
version = "1.0"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "ktca-core"
            version = version.toString()
        }
    }
}