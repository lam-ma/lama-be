import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
    application
    idea
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

group = "com.lama"
version = "1.0"


repositories {
    jcenter()
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlinx") }
    maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
}

dependencies {
    implementation("io.vertx:vertx-web:4.0.0")
    implementation("io.vertx:vertx-web-client:4.0.0")
    implementation("io.vertx:vertx-lang-kotlin:4.0.0")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.0.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
    implementation("com.fasterxml.jackson.module:jackson-modules-java8:2.11.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.2")

    implementation("org.apache.logging.log4j:log4j-core:2.13.3")
    implementation("org.apache.logging.log4j:log4j-api:2.13.3")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.3")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("io.github.microutils:kotlin-logging:1.8.3")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.assertj:assertj-core:3.16.1")
    testImplementation("com.jayway.jsonpath:json-path:2.4.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        apiVersion = "1.4"
        languageVersion = "1.4"
        freeCompilerArgs += "-Xopt-in=kotlin.time.ExperimentalTime"
    }
}

application {
    mainClassName = "com.lama.MainKt"
}

tasks.getByName<ShadowJar>("shadowJar") {
    archiveBaseName.set("lama")
    archiveClassifier.set("")
    archiveVersion.set("")
}
