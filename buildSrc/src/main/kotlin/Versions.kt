import org.gradle.api.JavaVersion

object Versions {
    val kotlin = "1.3.41"
    val coroutines = "1.2.2"
    val ktor = "1.2.3"
    val serialization = "0.11.1"

    val javaVersion = JavaVersion.VERSION_1_8
    val jvmTarget = javaVersion.toString()
    val targetJvmVersionAttribute = Integer.parseInt(javaVersion.majorVersion)
}
