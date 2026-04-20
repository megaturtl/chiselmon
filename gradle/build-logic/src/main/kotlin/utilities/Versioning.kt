package utilities

import org.gradle.api.Project

fun Project.isSnapshot(): Boolean = rootProject.property("snapshot").toString() == "true"

fun Project.writeVersion(type: VersionType = VersionType.FULL): String {
    val modVer = rootProject.property("mod_version").toString()
    val mcVer = rootProject.property("mc_version").toString()
    val baseVersion = "$modVer+$mcVer"

    return when (type) {
        // This is the "Discovery" version. ALWAYS use -SNAPSHOT for Maven.
        VersionType.PUBLISHING -> if (isSnapshot()) "$baseVersion-SNAPSHOT" else baseVersion
        // This is the "Identity" version. The build scripts will append the Git info.
        VersionType.FULL -> baseVersion
    }
}

enum class VersionType { PUBLISHING, FULL }