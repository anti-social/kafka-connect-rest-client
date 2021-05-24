import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version Versions.kotlin
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

repositories {
    mavenCentral()
}

group = "dev.evo.kafka-connect-rest-client"
version = "0.0.8"

kotlin {
    jvm {
        compilations {
            listOf(this["main"], this["test"]).forEach {
                it.kotlinOptions {
                    jvmTarget = Versions.jvmTarget
                }
            }
        }
        attributes {
            attribute(
                TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,
                Versions.targetJvmVersionAttribute
            )
        }
    }
    js {
        nodejs()
        compilations.all {
            kotlinOptions {
                moduleKind = "umd"
                sourceMap = true
            }
        }
    }
    linuxX64 {
        binaries {
            executable {
                entryPoint = "dev.evo.kafka.connect.restclient.cli.main"
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(ktorClient("core"))
                implementation(ktorClient("json"))
                implementation(ktorClient("serialization"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(coroutines("core"))
                implementation(ktorClient("mock"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(ktorClient("cio"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        val jsMain by getting {}
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val nativeMain by creating {
            dependencies {
                implementation(ktorClient("curl"))
            }
        }
        val nativeTest by creating {}

        val nativeTargetNames = targets.withType<KotlinNativeTarget>().names
        project.configure(nativeTargetNames.map { getByName("${it}Main") }) {
            dependsOn(nativeMain)
        }
        project.configure(nativeTargetNames.map { getByName("${it}Test") }) {
            dependsOn(nativeTest)
        }
    }
}

configureMultiplatformPublishing(project.name, "Kafka connect rest client")
