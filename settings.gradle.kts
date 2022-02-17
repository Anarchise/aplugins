

rootProject.name = "APlugins"
include("ahotkeys")
include("apest")
include("ACrabs")
include("abankstander")
include("afood")
include("apker")
include("astuntelealch")
include("atempoross")
include(":autils")
include("askiller")
include("afighter")
include("arcer")
include("azulrah")
include("avorkath")
include("atablets")
include("apickpocket")

//include("anightmarezone")

//include("awyverns")
//include("amlm")
//include("aelves")
//include("ablast")

include("arunedragons")

for (project in rootProject.children) {
    project.apply {
        projectDir = file(name)
        buildFileName = "$name.gradle.kts"

        require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}