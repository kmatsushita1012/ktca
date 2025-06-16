plugins {
    kotlin("jvm") version "2.0.21"
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
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

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.symbol.processing.api)
    implementation(project(":core"))
}

// KSPプロセッサのエントリーポイントを登録するための設定（META-INF/servicesなど）は手動で準備するか、Gradleタスクで生成
