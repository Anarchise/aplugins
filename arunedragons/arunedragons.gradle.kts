
version = "1.0.8"

project.extra["PluginName"] = "Anarchise' Rune Dragon Plugin" // This is the name that is used in the external plugin manager panel
project.extra["PluginDescription"] = "Auto Rune Dragons." // This is the description that is used in the external plugin manager panel
project.extra["PluginSupportUrl"] = "https://discord.com/invite/KwJnhKQJVc"

dependencies {
    compileOnly(project(":autils"))
}

tasks {
    register<proguard.gradle.ProGuardTask>("proguard") {
        configuration("${project.buildDir}/config/proguard/proguard.txt")

        injars("${project.buildDir}/libs/${project.name}-${project.version}.jar")
        outjars("${project.buildDir}/libs/out/${project.name}-${project.version}.jar")

        target("11")

        adaptresourcefilenames()
        adaptresourcefilecontents()
        optimizationpasses(20)
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