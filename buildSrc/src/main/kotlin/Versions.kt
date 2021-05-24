import org.gradle.api.JavaVersion

object Versions {
    val kotlin = "1.4.32"
    val coroutines = "1.4.3-native-mt"
    val ktor = "1.5.2"
    val serialization = "1.1.0"

    val javaVersion = JavaVersion.VERSION_1_8
    val jvmTarget = javaVersion.toString()
    val targetJvmVersionAttribute = Integer.parseInt(javaVersion.majorVersion)
}
