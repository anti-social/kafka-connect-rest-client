import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization") version Versions.kotlin
    `maven-publish`
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
}
group = "dev.evo"
version = "0.0.3"

kotlin {
    jvm()
//    js {
//        nodejs()
//        compilations.all {
//            kotlinOptions {
//                moduleKind = "umd"
//                sourceMap = true
//            }
//        }
//    }
    linuxX64 {
        binaries {
            executable() {
                entryPoint = "dev.evo.kafka.connect.restclient.cli.main"
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(serialization("runtime-common"))
                implementation(ktorClient("core"))
                implementation(ktorClient("json"))
                implementation(ktorClient("serialization"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(coroutines("core-common"))
                implementation(ktorClient("mock"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(serialization("runtime"))
                implementation(ktorClient("cio"))
                implementation(ktorClient("serialization-jvm"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
                implementation(coroutines("core"))
                implementation(ktorClient( "mock-jvm"))
            }
        }

//        val jsMain by getting {
//            dependencies {
//                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
//
//                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:${Versions.serialization}")
//                implementation("io.ktor:ktor-client-json-js:${Versions.ktor}")
//                implementation("io.ktor:ktor-client-serialization-js:${Versions.ktor}")
//            }
//        }
//        val jsTest by getting {
//            dependencies {
//                implementation("org.jetbrains.kotlin:kotlin-test-js")
//
//                implementation("io.ktor:ktor-client-mock-js:${Versions.ktor}")
//            }
//        }

        val nativeMain by creating {
            dependencies {
                implementation(serialization("runtime-native"))
                implementation(ktorClient("core-native"))
                implementation(ktorClient("curl"))
                implementation(ktorClient("serialization-native"))
            }
        }
        val nativeTest by creating {
            dependencies {
                implementation(coroutines("core-native"))
                implementation(ktorClient("mock-native"))
            }
        }
        val nativeTargetNames = targets.withType<KotlinNativeTarget>().names
        project.configure(nativeTargetNames.map { getByName("${it}Main") }) {
            dependsOn(nativeMain)
        }
        project.configure(nativeTargetNames.map { getByName("${it}Test") }) {
            dependsOn(nativeTest)
        }
    }
}

publishing {
    repositories {
        bintray(project, "kafka-connect-rest-client")
    }
}
