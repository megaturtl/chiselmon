plugins {
    id("chiselmon.platform-conventions")
    id("chiselmon.publish-conventions")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    neoForge {

    }
}

repositories {
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://maven.impactdev.net/repository/development")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.turtl.cc/snapshots")
    maven("https://maven.turtl.cc/releases")
    mavenLocal()
}

dependencies {
    neoForge(libs.neoforge)

    implementation(libs.neo.kotlin.forge)

    implementation(libs.h2db)
    include(libs.h2db)

    runtimeOnly(libs.bundles.neoforge.integrations.runtimeOnly) // this has to be runtimeOnly instead of modRuntimeOnly or loom can't remap the quilt parsers correctly

    implementation(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    bundle(project(path = ":common", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }
    testImplementation(project(":common", configuration = "namedElements"))

}

tasks {
    shadowJar {
        exclude("architectury-common.accessWidener")
        exclude("architectury.common.json")

    }

    processResources {
        inputs.property("mod_id", project.property("mod_id"))
        inputs.property("mod_display_name", project.property("mod_display_name"))
        inputs.property("mod_author", project.property("mod_author"))
        inputs.property("mod_description", project.property("mod_description"))

        inputs.property("version", rootProject.version.toString())
        inputs.property("minecraft_version", rootProject.property("mc_version").toString())
        inputs.property("java_version", rootProject.property("java_version").toString())

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(
                "mod_id" to project.property("mod_id"),
                "mod_display_name" to project.property("mod_display_name"),
                "mod_author" to project.property("mod_author"),
                "mod_description" to project.property("mod_description"),

                "version" to rootProject.version.toString(),
                "minecraft_version" to rootProject.property("mc_version").toString(),
                "java_version" to rootProject.property("java_version").toString()
            )
        }
    }
}

tasks {
    sourcesJar {
        val depSources = project(":common").tasks.sourcesJar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(depSources)
        from(depSources.get().archiveFile.map { zipTree(it) }) {
            exclude("architectury.accessWidener")
        }
    }
}

// Stole from architect discord, replaces loom.forge.convertAccessWideners
tasks.remapJar {
    atAccessWideners.add("chiselmon-common.accesswidener")
}