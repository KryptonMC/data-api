import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.spring") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"

    id("org.springframework.boot") version "2.5.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

group = "org.kryptonmc"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_16

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.1.0")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springdoc:springdoc-openapi-ui:1.5.8")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.2")
}

configurations.all {
    exclude("org.springframework.boot", "spring-boot-starter-logging") // Goodbye Logback, hello Log4J 2
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "16"
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
    withType<BootJar> {
        archiveFileName.set("DataAPI-${project.version.toString()}.jar")
    }
}
