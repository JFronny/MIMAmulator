plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "de.frohnmeyer-wds"

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