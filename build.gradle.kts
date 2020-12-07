import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
    application
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
    implementation("io.vertx:vertx-web:4.0.0.CR2")
    implementation("io.vertx:vertx-web-client:4.0.0.CR2")
    implementation("io.vertx:vertx-lang-kotlin:4.0.0.CR2")
    implementation("io.vertx:vertx-lang-kotlin-coroutines:4.0.0.CR2")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions {
        jvmTarget = "11"
        apiVersion = "1.4"
        languageVersion = "1.4"
    }
}

application {
    mainClassName = "ServerKt"
}
