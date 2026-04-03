plugins {
    base
    id("net.nemerosa.versioning")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}
