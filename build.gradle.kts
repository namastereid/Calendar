import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.serialization") version "1.4.0"
    java
    application
}

group = "me.namastereid"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("io.kotest:kotest-assertions-core:4.6.0") // for kotest core jvm assertions
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    implementation("io.ktor:ktor-server-netty:1.4.0")
    implementation("io.ktor:ktor-html-builder:1.4.0")
    implementation("io.ktor:ktor-serialization:1.4.0")
//    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("org.slf4j:slf4j-simple:1.7.30")
    implementation("com.google.api-client:google-api-client:1.23.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
    implementation("com.google.apis:google-api-services-calendar:v3-rev305-1.23.0")
    implementation("com.google.guava:guava:30.1.1-jre")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
//    mainClass.set("nam.calendar.ApplicationKt.module")
//    mainClass.set("CalendarQuickstart")
    mainClass.set("io.ktor.server.netty.EngineMain")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}