import utilities.isSnapshot
import utilities.writeVersion

plugins {
    id("chiselmon.root-conventions")
}

// Start with the base: 1.0.1+1.21.1
val base = project.writeVersion(utilities.VersionType.FULL)

// Append the Git info ONLY if it's a snapshot
version = if (project.isSnapshot()) {
    val branch = versioning.info.branch.substringAfter("/")
    "$base-$branch-${versioning.info.build}"
} else {
    base
}

subprojects {
    configurations.all {
        resolutionStrategy {
            // This forces every sub-module (common, fabric, neoforge) to check for fresh snapshots on every build.
            cacheChangingModulesFor(0, "seconds")
        }
    }
}