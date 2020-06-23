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
    compile("com.microsoft.sqlserver:mssql-jdbc:8.2.2.jre8")
    implementation("org.slf4j", "slf4j-api", "1.7.25")
    implementation("org.slf4j", "slf4j-simple", "1.7.25")
}
