import utilities.writeVersion

plugins {
    id("chiselmon.root-conventions")
}

version = project.writeVersion()

tasks.register("printVersion") {
    description = "Prints the configured Chiselmon version."
    doLast {
        println(project.version)
    }
}

subprojects {
    configurations.all {
        resolutionStrategy {
            // This forces every sub-module (common, fabric, neoforge) to check for fresh snapshots on every build.
            cacheChangingModulesFor(0, "seconds")
        }
    }
}