plugins {
    kotlin("jvm") version "2.0.0"
    java
    `maven-publish`
}

group = "dev.rakiiii.healthycoroutines"
version = "1.0.1"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("io.gitlab.arturbosch.detekt:detekt-api:1.23.7")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.23.7")
    testImplementation("io.gitlab.arturbosch.detekt:detekt-rules-libraries:1.23.7")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
    from("LICENCE.txt") {
        into("META-INF")
    }
}

publishing {

    publications {

        register("release", MavenPublication::class) {

            groupId = "dev.rakiiii"

            artifactId = "healthycoroutines"

            version = "1.0.1"

            from(components["java"])

            artifact(sourcesJar)

            pom {
                packaging = "jar"
                name.set("healthycoroutines")
                description.set("healthycoroutines")
            }

        }

    }

    repositories {
        maven { url = uri("https://jitpack.io") }
    }

}