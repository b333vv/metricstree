/*
 * Copyright 2020 b333vv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//import org.jetbrains.intellij.intellij
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("org.jetbrains.intellij") version "1.17.4"
}

fun properties(key: String) = project.findProperty(key).toString()

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

configurations {
    val testImplementation = configurations.getByName("testImplementation")
    val testRuntimeOnly = configurations.getByName("testRuntimeOnly")

    create("integrationTestImplementation") {
        extendsFrom(testImplementation)
    }
    create("integrationTestRuntimeOnly") {
        extendsFrom(testRuntimeOnly)
    }
    create("e2eTestImplementation") {
        extendsFrom(testImplementation)
    }
    create("e2eTestRuntimeOnly") {
        extendsFrom(testRuntimeOnly)
    }
}

sourceSets {
    val main by getting
    test {
        java.srcDir("src/test/java")
        resources.srcDir("src/test/resources")
    }
    create("integrationTest") {
        compileClasspath += main.output
        runtimeClasspath += main.output
        java.srcDir("src/integration-test/java")
        resources.srcDir("src/integration-test/resources")
    }
    create("e2eTest") {
        compileClasspath += main.output
        runtimeClasspath += main.output
        java.srcDir("src/e2e-test/java")
        resources.srcDir("src/e2e-test/resources")
    }
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
    }

    dependencies {
        implementation("org.knowm.xchart:xchart:3.6.3")
        implementation("org.json:json:20211205")
//        testImplementation ("junit:junit:4.9")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.11.0")
        testImplementation("org.assertj:assertj-core:3.6.2")
        testImplementation("org.mockito:mockito-core:5.11.0")
        testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
        implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.10")
        testImplementation(project(":metric-verification-data"))
        // Kotlin-specific verification data module for tests
        testImplementation(project(":metric-verification-data-kotlin"))
        add("integrationTestImplementation", project(":metric-verification-data-kotlin"))
    }
    register("integrationTest", org.gradle.api.tasks.testing.Test::class.java) {
        description = "Runs integration tests."
        group = "verification"
        testClassesDirs = project.sourceSets["integrationTest"].output.classesDirs
        classpath = project.sourceSets["integrationTest"].runtimeClasspath
        mustRunAfter(named("test"))
        // Исключаем тесты из пакета org.b333vv.metric.research
        exclude("org/b333vv/metric/research/**")
    }
    register("e2eTest", org.gradle.api.tasks.testing.Test::class.java) {
        description = "Runs end-to-end tests."
        group = "verification"
        testClassesDirs = project.sourceSets["e2eTest"].output.classesDirs
        classpath = project.sourceSets["e2eTest"].runtimeClasspath
        mustRunAfter(named("integrationTest"))
    }

    check {
        dependsOn(named("integrationTest"))
        dependsOn(named("e2eTest"))
    }

    named("classpathIndexCleanup") {
        mustRunAfter(named("compileIntegrationTestJava"))
        mustRunAfter(named("compileE2eTestJava"))
    }
}