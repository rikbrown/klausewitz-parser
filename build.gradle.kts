import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.10"
    antlr
    idea
}

group = "codes.rik.klausewitz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    // 1P
    implementation("codes.rik", "kotlin-pieces", "0.+")

    // 3P
    implementation("com.google.guava", "guava", "27.0.1-jre")
    implementation("org.antlr", "antlr4-runtime", "4.7.2")

    // Antlr
    antlr("org.antlr", "antlr4", "4.7.2")

    // Test
    testImplementation("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val generateGrammarSource: AntlrTask by tasks
generateGrammarSource.apply {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-package", "codes.rik.klausewitz.antlr")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}