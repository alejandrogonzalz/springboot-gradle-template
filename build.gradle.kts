plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    jacoco
    id("checkstyle")
    id("com.diffplug.spotless") version "6.23.3"
    id("com.github.jakemarsden.git-hooks") version "0.0.2"
}

group = "com.example"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Database - MySQL
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Flyway for DB migrations
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // JWT Authentication
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // Phone number validation and formatting
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.27")

    // Lombok for reducing boilerplate
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Mapping utilities
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    // OpenAPI/Swagger Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // Utils
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("com.google.guava:guava:32.1.3-jre")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:mysql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("io.rest-assured:rest-assured:5.4.0")

    // For Lombok in tests
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/config/**",
                    "**/dto/**",
                    "**/entity/**",
                    "**/exception/**",
                    "**/*Application.class"
                )
            }
        })
    )
}

checkstyle {
    toolVersion = "10.12.4"
    configFile = file("${projectDir}/config/checkstyle/checkstyle.xml")
}

tasks.withType<Checkstyle> {
    reports {
        xml.required.set(false)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.file("reports/checkstyle/${name}.html"))
    }
    configFile = file("${projectDir}/config/checkstyle/checkstyle.xml")
    isIgnoreFailures = true
    maxWarnings = 100
    
    doLast {
        val reportFile = reports.html.outputLocation.get().asFile
        if (reportFile.exists()) {
            println("\n✅ Checkstyle report: file://${reportFile.absolutePath}")
        }
    }
}

spotless {
    java {
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

gitHooks {
    setHooks(mapOf(
        "pre-commit" to "preCommit",
        "commit-msg" to """
            #!/bin/bash
            commit_msg=${'$'}(cat ${'$'}1)
            if ! echo "${'$'}commit_msg" | grep -qE "^(feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert)(\(.+\))?: [A-Z].{0,99}${'$'}"; then
                echo "❌ Invalid commit message format!"
                echo "Format: <type>(scope): <Subject starting with capital letter>"
                echo "Types: feat, fix, docs, style, refactor, test, chore, perf, ci, build, revert"
                exit 1
            fi
        """.trimIndent()
    ))
}


tasks.test {
    doLast {
        val reportPath = file("build/reports/tests/test/index.html").absolutePath
        println("\n📊 Test report: file://$reportPath")
    }
}

tasks.clean {
    doLast {
        val logsDir = file("${projectDir}/logs")
        if (logsDir.exists()) {
            logsDir.listFiles()?.filter { it.extension == "log" }?.forEach { it.delete() }
        }
        println("Logs deleted successfully")
    }
}


tasks.register("preCommit") {
    dependsOn("spotlessCheck", "test")
    group = "verification"
    description = "Runs spotless formatting and unit tests"
}

tasks.register("generateOpenApiSpec") {
    group = "documentation"
    description = "Generate OpenAPI spec file and Zod schemas for frontend"
    
    doLast {
        val outputDir = file("src/api")
        outputDir.mkdirs()
        
        println("📥 Downloading OpenAPI spec...")
        exec {
            commandLine("curl", "http://localhost:8080/v3/api-docs", "-o", "openapi.json")
        }
        
        println("🔧 Generating Zod schemas...")
        exec {
            commandLine("npx", "openapi-zod-client", "openapi.json", "-o", "src/api/schemas.ts")
        }

        exec {
            commandLine("rm", "openapi.json")
        }
        
        println("✅ Generated:")
    }
}
