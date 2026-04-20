import utilities.VersionType
import utilities.isSnapshot
import utilities.writeVersion

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("dev.architectury.loom")
    id("net.nemerosa.versioning")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            val snapshot = project.isSnapshot()

            val releases = uri("https://maven.turtl.cc/releases")
            val snapshots = uri("https://maven.turtl.cc/snapshots")

            url = if (snapshot) snapshots else releases
            name = "turtl"
            credentials {
                username = System.getenv("MAVEN_USER")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>(project.name) {
            artifact(tasks.remapJar)
            artifact(tasks.remapSourcesJar)

            @Suppress("UnstableApiUsage")
            loom.disableDeprecatedPomGeneration(this)

            groupId = "cc.turtl.chiselmon"
            artifactId = project.findProperty("maven.artifactId")?.toString() ?: project.name

            version = project.writeVersion(VersionType.PUBLISHING)

            pom {
                properties.set(mapOf(
                    "gitBranch" to versioning.info.branch,
                    "gitCommit" to versioning.info.commit,
                    "buildNumber" to versioning.info.build.toString()
                ))
            }
        }
    }
}