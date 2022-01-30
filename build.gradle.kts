import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
    id("org.springframework.boot") version "2.6.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.diffplug.spotless") version "6.1.0"
    id("me.qoomon.git-versioning") version "5.1.4"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "io.github.carlomicieli"
version = "0.0.0-SNAPSHOT"
gitVersioning.apply {
    refs {
        branch("main") {
            version = "\${commit.timestamp}-\${commit.short}"
        }
        tag("v(?<version>.*)") {
            version = "\${ref.version}"
        }
    }

    rev {
        version = "\${commit.short}-SNAPSHOT"
    }
}

configurations {
    all {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    implementation {
        resolutionStrategy {
            failOnVersionConflict()
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

extra["testcontainersVersion"] = "1.16.2"

dependencies {
    constraints {
        implementation("org.apache.logging.log4j:log4j-core") {
            version {
                strictly("[2.17, 3[")
                prefer("2.17.0")
            }
            because("CVE-2021-44228, CVE-2021-45046, CVE-2021-45105: Log4j vulnerable to remote code execution and other critical security vulnerabilities")
        }
    }

    modules {
        module("org.springframework.boot:spring-boot-starter-tomcat") {
            replacedBy("org.springframework.boot:spring-boot-starter-reactor-netty", "Use Netty instead of Tomcat")
        }
    }

    implementation("com.auth0:java-jwt:3.18.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework:spring-jdbc")
    runtimeOnly("io.r2dbc:r2dbc-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.0.1")
    runtimeOnly("com.fasterxml.jackson.core:jackson-databind")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("io.kotest:kotest-assertions-core:5.1.0")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        val integrationTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project)

                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("io.projectreactor:reactor-test")
                implementation("org.springframework.security:spring-security-test")
                implementation("org.testcontainers:junit-jupiter")
                implementation("io.kotest:kotest-assertions-core:5.1.0")
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                        maxHeapSize = "512m"
                        testLogging {
                            exceptionFormat = FULL
                        }
                    }
                }
            }
        }
    }
}

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
    }
}

tasks.getByName<BootBuildImage>("bootBuildImage") {
    imageName = "carlomicieli/spring-boot-app:${project.version}"
    tags = listOf("carlomicieli/spring-boot-app:latest")
}

tasks {
    withType<JavaCompile> {
        options.isIncremental = true
        options.isFork = true
        options.isFailOnError = false
    }

    withType<KotlinCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()

        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = JavaVersion.VERSION_17.toString()
            apiVersion = "1.6"
            languageVersion = "1.6"
        }
    }

    test {
        useJUnitPlatform()

        maxHeapSize = "1G"
        failFast = false

        testLogging {
            showStandardStreams = false
            events(PASSED, FAILED, SKIPPED)
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = FULL
        }
    }
}

spotless {
    kotlin {
        endWithNewline()
        ktlint()
        toggleOffOn("fmt:off", "fmt:on")
        indentWithSpaces()
        trimTrailingWhitespace()
        licenseHeaderFile("${project.rootDir}/config/LICENSE")
    }

    kotlinGradle {
        endWithNewline()
        ktlint()
        indentWithSpaces()
        trimTrailingWhitespace()
    }

    tasks.check {
        dependsOn(tasks.spotlessCheck)
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}
