import org.gradle.api.JavaVersion
import org.gradle.api.plugins.JavaApplication
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    // application
    kotlin("multiplatform")
    kotlin("plugin.serialization") version Versions.kotlin
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
    id("org.ajoberstar.grgit") version Versions.grgit
}

repositories {
    mavenCentral()
}

group = "dev.evo.kafka-connect-rest-client"

val gitDescribe = grgit.describe(mapOf("tags" to true, "match" to listOf("v*")))
    ?: "v0.0.0-unknown"
version = gitDescribe.trimStart('v')

kotlin {
    jvm {
        // withJava()
        // configure<JavaApplication> {
        //     mainClass.set("dev.evo.kafka.connect.restclient.cli.MainKt")
        // }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    js {
        nodejs()
        compilerOptions {
            moduleKind.set(JsModuleKind.MODULE_UMD)
            sourceMap.set(true)

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
                implementation(ktorClient("content-negotiation"))
                implementation(ktorSerialization("kotlinx-json"))
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
    }
}

configureMultiplatformPublishing(project.name, "Kafka connect rest client")
