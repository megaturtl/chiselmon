package utilities

import org.gradle.api.Project

fun Project.isSnapshot(): Boolean = rootProject.property("snapshot").toString() == "true"

fun Project.cobblemonVersion(): String = rootProject.property("cobblemon_version").toString()

fun Project.cobblemonDependency(module: String): String =
    "com.cobblemon:$module:${cobblemonVersion()}+${rootProject.property("mc_version")}"

fun Project.cobblemonFabricVersionRange(): String = "~${cobblemonVersion()}"

fun Project.cobblemonNeoForgeVersionRange(): String {
    val components = cobblemonVersion().split(".")
    require(components.size == 3) {
        "cobblemon_version must use major.minor.patch, got ${cobblemonVersion()}"
    }

    val major = components[0].toIntOrNull()
    val minor = components[1].toIntOrNull()
    require(major != null && minor != null) {
        "cobblemon_version must use numeric major and minor components, got ${cobblemonVersion()}"
    }

    return "[${cobblemonVersion()},$major.${minor + 1}.0)"
}

fun Project.writeVersion(type: VersionType = VersionType.FULL): String {
    val releaseVersion = "${rootProject.property("chiselmon_version")}+cobblemon-${cobblemonVersion()}"
    val buildMetadata =
        rootProject
            .findProperty("build_metadata")
            ?.toString()
            ?.takeIf(String::isNotBlank)
            ?.let { ".$it" }
            .orEmpty()
    val fullVersion = "$releaseVersion$buildMetadata"

    return when (type) {
        VersionType.PUBLISHING -> if (isSnapshot()) "$fullVersion-SNAPSHOT" else fullVersion
        VersionType.FULL -> fullVersion
    }
}

enum class VersionType { PUBLISHING, FULL }
