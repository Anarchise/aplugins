
import ProjectVersions.openosrsVersion
version = "1.0.4"

project.extra["PluginName"] = "AUtils"
project.extra["PluginDescription"] = "Tools required for APlugins to function."


dependencies {

    annotationProcessor(Libraries.lombok)
    annotationProcessor(Libraries.pf4j)

    compileOnly("com.openosrs:runelite-api:4.17.1")
    compileOnly("com.openosrs:runelite-client:4.17.1")

    compileOnly(Libraries.guice)
    //  compileOnly(Libraries.javax)
    compileOnly(Libraries.lombok)
    compileOnly(Libraries.pf4j)
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}