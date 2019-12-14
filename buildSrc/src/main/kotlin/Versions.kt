import org.gradle.api.JavaVersion

object Versions {
    val kotlin = "1.3.60"
    val coroutines = "1.3.2"
    val ktor = "1.2.6"
    val serialization = "0.14.0"

    val javaVersion = JavaVersion.VERSION_1_8
    val jvmTarget = javaVersion.toString()
    val targetJvmVersionAttribute = Integer.parseInt(javaVersion.majorVersion)
}
