import java.net.URI

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private const val bintrayUsername = "evo"
private const val bintrayRepoName = "maven"

fun Project.bintrayUrl(packageName: String): URI {
    val bintrayPublish = findProperty("bintrayPublish")?.toString()
        ?: System.getenv("BINTRAY_PUBLISH")
        ?: "0"
    return URI(
        "https://api.bintray.com/maven/$bintrayUsername/$bintrayRepoName/$packageName/;publish=$bintrayPublish"
    )
}

fun Project.bintrayUser(): String? {
    return findProperty("bintrayUser")?.toString()
        ?: System.getenv("BINTRAY_USER")
}

fun Project.bintrayApiKey(): String? {
    return findProperty("bintrayApiKey")?.toString()
        ?: System.getenv("BINTRAY_API_KEY")
}

fun RepositoryHandler.bintray(project: Project, packageName: String? = null): MavenArtifactRepository = maven {
    name = "bintray"
    url = project.bintrayUrl(packageName ?: project.rootProject.name)
    credentials {
        username = project.bintrayUser()
        password = project.bintrayApiKey()
    }
}

fun RepositoryHandler.test(project: Project): MavenArtifactRepository = maven {
    name = "test"
    url = project.uri("file://${project.rootProject.buildDir}/localMaven")
}

fun PublishingExtension.configureRepositories(project: Project, packageName: String? = null) = repositories {
    bintray(project, packageName)
    test(project)
}

fun PublishingExtension.configureMultiplatformPublishing(project: Project, packageName: String? = null) {
    val emptyJar by project.tasks.register<Jar>("emptyJar")
    val sourcesJar by project.tasks.register<Jar>("sourcesJar") {
        val kotlin = project.extensions.getByName<KotlinMultiplatformExtension>("kotlin")
        from(kotlin.sourceSets.named("commonMain").get().kotlin)
        archiveClassifier.set("sources")
    }
    publications.getByName<MavenPublication>("kotlinMultiplatform") {
        artifactId = "${project.name}-native"
        artifact(emptyJar)
        artifact(sourcesJar)
    }

    configureRepositories(project, packageName)
}
