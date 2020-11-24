import org.gradle.api.JavaVersion

object Versions {
    val kotlin = "1.3.72"
    val coroutines = "1.3.8"
    val ktor = "1.3.2"
    val serialization = "0.20.0"

    val javaVersion = JavaVersion.VERSION_1_8
    val jvmTarget = javaVersion.toString()
    val targetJvmVersionAttribute = Integer.parseInt(javaVersion.majorVersion)
}
