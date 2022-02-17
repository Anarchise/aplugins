
version = "1.0.8"

project.extra["PluginName"] = "AStunTeleAlch"
project.extra["PluginDescription"] = "Anarchise' Stun/Tele Alcher."
project.extra["PluginProvider"] = "Anarchise"
project.extra["PluginSupportUrl"] = "https://discord.com/invite/KwJnhKQJVc"

dependencies {
    compileOnly(project(":autils"))

    compileOnly("com.openosrs:runelite-api:4.12.0")
    compileOnly("com.openosrs:runelite-client:4.12.0")
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {

        classpath("com.android.tools.build:gradle:3.0.0")
    }
}
tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Dependencies" to
                            arrayOf(
                                    nameToId("AUtils")).joinToString(),
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}