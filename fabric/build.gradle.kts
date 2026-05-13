configurations.all {
    resolutionStrategy {
        force(libs.fabric.loader)
    }
}

plugins {
    id("chiselmon.platform-conventions")
    id("chiselmon.publish-conventions")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val generatedResources = file("src/generated/resources")

sourceSets.main {
    resources {
        srcDir(generatedResources)
    }
}

repositories {
    mavenLocal()
    maven("https://api.modrinth.com/maven")
    maven("https://maven.impactdev.net/repository/development")
    maven("https://maven.terraformersmc.com/")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.turtl.cc/snapshots")
    maven("https://maven.turtl.cc/releases")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

dependencies {
    implementation(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    bundle(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }
    modImplementation(libs.fabric.loader)
    modApi(libs.fabric.api)

    implementation(libs.h2db)

    // Jar in jar
    include(libs.h2db)
    include(libs.turtlshell.fabric)

    modCompileOnly(libs.bundles.common.integrations.compileOnly) {
        isTransitive = false
    }

    modRuntimeOnly(libs.bundles.fabric.integrations.runtimeOnly)

    modRuntimeOnly(libs.turtlshell.fabric) {isChanging = true} // gets the latest snapshot

    modImplementation(libs.bundles.fabric.integrations.implementation)
}

tasks {
    // The AW file is needed in :fabric project resources when the game is run.
    val copyAccessWidener by registering(Copy::class) {
        from(loom.accessWidenerPath)
        into(generatedResources)
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(copyAccessWidener)

        inputs.property("mod_id", project.property("mod_id"))
        inputs.property("mod_display_name", project.property("mod_display_name"))
        inputs.property("mod_author", project.property("mod_author"))
        inputs.property("mod_description", project.property("mod_description"))

        inputs.property("version", rootProject.version.toString())
        inputs.property("fabric_loader_version", libs.fabric.loader.get().version)
        inputs.property("fabric_api_version", libs.fabric.api.get().version)
        inputs.property("fabric_kotlin_version", libs.fabric.kotlin.get().version)
        inputs.property("minecraft_version", rootProject.property("mc_version").toString())
        inputs.property("java_version", rootProject.property("java_version").toString())

        filesMatching("fabric.mod.json") {
            expand(
                "mod_id" to project.property("mod_id"),
                "mod_display_name" to project.property("mod_display_name"),
                "mod_author" to project.property("mod_author"),
                "mod_description" to project.property("mod_description"),
                "version" to rootProject.version.toString(),
                "fabric_loader_version" to libs.fabric.loader.get().version,
                "fabric_api_version" to libs.fabric.api.get().version,
                "fabric_kotlin_version" to libs.fabric.kotlin.get().version,
                "minecraft_version" to rootProject.property("mc_version").toString(),
                "java_version" to rootProject.property("java_version").toString()
            )
        }
    }

    sourcesJar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(copyAccessWidener)
    }
}
