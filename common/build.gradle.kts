import utilities.isSnapshot
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    id("chiselmon.base-conventions")
    id("chiselmon.publish-conventions")

    id("net.kyori.blossom")
    id("org.jetbrains.gradle.plugin.idea-ext")
}

architectury {
    common("neoforge", "fabric")
}

repositories {
    maven("https://api.modrinth.com/maven")
    maven("https://maven.impactdev.net/repository/development")
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.gegy.dev")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.turtl.cc/snapshots")
    maven("https://maven.turtl.cc/releases")
    mavenLocal()
}

configurations.all {
    resolutionStrategy {
        // Force Gradle to check for a new snapshot every build
        cacheChangingModulesFor(0, "seconds")
    }
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.h2db)
    modImplementation(libs.fabric.loader)

    modCompileOnly(libs.bundles.common.integrations.compileOnly) {
        isTransitive = false
    }


    // Unit Testing
    testImplementation(libs.bundles.unitTesting)
    testImplementation(libs.cobblemon.common)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        setEvents(listOf("failed"))
        setExceptionFormat("full")
    }
}

sourceSets {
    main {
        blossom {
            kotlinSources {
                property("mod_id", project.property("mod_id").toString())
                property("mod_display_name", project.property("mod_display_name").toString())
                property("mod_author", project.property("mod_author").toString())
                property("mod_version", rootProject.version.toString())
                property("isSnapshot", if (rootProject.isSnapshot()) "true" else "false")
                property("gitCommit", versioning.info.commit)
                property("branch", versioning.info.branch)
                System.getProperty("buildNumber")?.let { property("buildNumber", it) }
                property("timestamp",
                    OffsetDateTime.now(ZoneOffset.UTC)
                        .format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss")) + " UTC"
                )
            }
        }
    }
}

