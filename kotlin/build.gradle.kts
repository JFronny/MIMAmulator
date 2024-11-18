plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "de.frohnmeyer-wds"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "de.frohnmeyer_wds.MainKt"
}