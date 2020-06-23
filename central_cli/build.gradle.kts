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
}
