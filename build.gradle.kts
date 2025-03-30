import org.gradle.api.tasks.SourceSetContainer

plugins {
    kotlin("jvm") version "1.9.0"
    id("java")                            // 꼭 필요!
    id("me.champeau.jmh") version "0.7.2" // jmh 플러그인
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
    jmhImplementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0") // 👈 이거 추가

    implementation("io.arrow-kt:arrow-core:1.2.4")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.4")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.2.4")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    jmhImplementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
}
kotlin {
    jvmToolchain(17)
}

// ★ jmh 소스셋이 이미 존재하므로, 여기서 "sourceSets { jmh { ... } }" 가능 ★
sourceSets {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
    named("jmh") {
        java.srcDir("src/jmh/kotlin")
    }
}

// JMH 설정(벤치마크 실행 옵션 등)
jmh {
    duplicateClassesStrategy = DuplicatesStrategy.WARN
    failOnError.set(true)
}