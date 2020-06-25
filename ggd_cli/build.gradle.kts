plugins {
    application
    kotlin("jvm")
}

application {
    mainClassName = "cli.Main"
}

dependencies {
    compile(project(":core"))
    compile(kotlin("stdlib"))
    implementation("org.slf4j", "slf4j-api", "1.7.25")
    implementation("org.slf4j", "slf4j-simple", "1.7.25")
}
