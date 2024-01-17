plugins {
    kotlin("jvm") version "1.9.22"
    id("me.champeau.jmh") version "0.7.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
    implementation("org.openjdk.jmh:jmh-core:1.37")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.22")
}

group = "com.github.corestone"
version = "0.0.1-SNAPSHOT"
description = "benchmark"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
