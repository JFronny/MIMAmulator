plugins {
    `java-library`
    kotlin("jvm") version "2.0.21"
    application
    id("org.graalvm.buildtools.native") version "0.10.3"
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
    mainClass = "de.frohnmeyerwds.mima.MainKt"
}

graalvmNative {
    binaries {
        named("main") {
            buildArgs.add("-O4")
            javaLauncher = javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(21)
                vendor = JvmVendorSpec.matching("Oracle Corporation")
            }
            sharedLibrary = false
        }
    }
}