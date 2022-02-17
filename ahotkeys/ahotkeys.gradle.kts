

version = "1.0.1"

project.extra["PluginName"] = "AHotkey" // This is the name that is used in the external plugin manager panel
project.extra["PluginDescription"] = "Anarchise' Widget Hotkeys" // This is the description that is used in the external plugin manager panel
project.extra["PluginSupportUrl"] = "https://discord.com/invite/KwJnhKQJVc"

dependencies {
    compileOnly(project(":autils"))
}

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    dependencies {
        // TODO: required due to https://github.com/Guardsquare/proguard/issues/30
        classpath("com.android.tools.build:gradle:3.0.0")

        classpath("com.guardsquare:proguard-gradle:7.0.1")
    }
}
tasks {
    register<proguard.gradle.ProGuardTask>("proguard") {
        configuration("${rootProject.projectDir}/config/proguard/proguard.txt")

        injars("${project.buildDir}/libs/${project.name}-${project.version}.jar")
        outjars("${project.buildDir}/libs/out/${project.name}-${project.version}.jar")

        target("11")

        adaptresourcefilenames()
        adaptresourcefilecontents()
        optimizationpasses(9)
        allowaccessmodification()
        mergeinterfacesaggressively()
        renamesourcefileattribute("SourceFile")
        keepattributes("Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod")

        libraryjars(System.getProperty("java.home") + "/jmods")
        libraryjars(configurations.compileClasspath.get())
    }
    jar {
        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Dependencies" to
                        arrayOf(
                            nameToId("AUtils")
                        ).joinToString(),
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}