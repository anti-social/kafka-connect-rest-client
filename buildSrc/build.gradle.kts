plugins {
    `kotlin-dsl`
    idea
}

repositories {
    mavenLocal()
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
}

idea {
    module {
        isDownloadJavadoc = false
        isDownloadSources = false
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    implementation("io.github.gradle-nexus:publish-plugin:1.1.0")
}
