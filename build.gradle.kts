plugins {
    base
    kotlin("jvm") version "1.3.70" apply false
    java
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

allprojects {


    apply(plugin="java")
    apply(plugin="com.github.johnrengelman.shadow")

    group = "org.dockbox.corona"
    version = "1.0"

    repositories {
        jcenter()
    }

    tasks {
        build {
            dependsOn(shadowJar)
        }
    }
}

dependencies {
    // Make the root project archives configuration depend on every subproject
    subprojects.forEach {
        archives(it)
    }
}
