import utilities.VersionType
import utilities.writeVersion

plugins {
    id("chiselmon.base-conventions")
    id("com.github.johnrengelman.shadow")
}

writeVersion(type = VersionType.FULL)

val bundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

// Read common's resources directly from source so the IDE doesn't need
// processResources to have run before launching.
sourceSets.main {
    resources.srcDir(project(":common").file("src/main/resources"))
}

loom {
    val clientConfig = runConfigs.getByName("client")
    clientConfig.runDir = "runClient"
//    clientConfig.vmArg("-Dmixin.debug=true")
    val serverConfig = runConfigs.getByName("server")
    serverConfig.runDir = "runServer"

    // Forge established the "main" name convention we're using here. Since NeoForged already defines it we must use
    // `maybeCreate`
    mods.maybeCreate("main")
    // This configurations ensures that the code and resources in :common are available to the platform-specific builds
    // in specifically the development mode. This code does nothing for the final packed jar since that one uses shadow
    // bundling / jar-in-jar magic to make sure the platform-specific jar has all relevant files available.
    mods.named("main") {
        sourceSet(project.sourceSets.main.get())
        sourceSet(project(":common").sourceSets.main.get())
    }
}

tasks {

    jar {
        archiveBaseName.set("Chiselmon-${project.name}")
        archiveClassifier.set("dev-slim")
    }

    shadowJar {
        archiveClassifier.set("dev-shadow")
        archiveBaseName.set("Chiselmon-${project.name}")
        configurations = listOf(bundle)
        mergeServiceFiles()
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        archiveBaseName.set("Chiselmon-${project.name}")
        archiveVersion.set("${rootProject.version}")
    }

    val copyJar by registering(CopyFile::class) {
        val productionJar = tasks.remapJar.flatMap { it.archiveFile }
        fileToCopy = productionJar
        destination = productionJar.flatMap {
            rootProject.layout.buildDirectory.file("libs/${it.asFile.name}")
        }
    }

    assemble {
        dependsOn(copyJar)
    }

}
