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
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    implementation("io.github.gradle-nexus:publish-plugin:2.0.0")
}
