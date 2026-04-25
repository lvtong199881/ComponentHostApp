// 组件依赖配置脚本
// 源码依赖模式：modules.json 存在时自动启用
// Maven 依赖模式：从 GitHub Packages 获取组件

val componentVersion = providers.gradleProperty("component.version").orNull ?: "1.0.0"
val componentGroupId = providers.gradleProperty("component.groupId").orNull ?: "com.mohanlv.component"

// 检测源码依赖模式（modules.json 存在且有配置）
val modulesFile = file("modules.json")
var useSourceDependency = false
val modulePaths = mutableMapOf<String, String>()

if (modulesFile.exists()) {
    try {
        val json = groovy.json.JsonSlurper().parse(modulesFile) as Map<*, *>
        useSourceDependency = json.isNotEmpty()
        json.forEach { (key, value) ->
            if (key is String && value is String) {
                modulePaths[key] = value
            }
        }
    } catch (e: Exception) {
        println("Warning: Failed to parse modules.json: ${e.message}")
    }
}
val modeName = if (useSourceDependency) "SOURCE (modules.json)" else "MAVEN (GitHub Packages)"

println("========================================")
println("ComponentHostApp Dependency Mode")
println("   Mode: $modeName")
println("   Version: $componentVersion")
println("   GroupId: $componentGroupId")
println("========================================")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        if (!useSourceDependency) {
            // Maven 依赖模式才需要 GitHub Packages
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/lvtong199881/AndroidComponentApp")
                credentials {
                    username = "lvtong199881"
                    password = System.getenv("GITHUB_TOKEN") ?: run {
                        val tokenFile = java.io.File(System.getProperty("user.home"), ".github_token")
                        if (tokenFile.exists()) tokenFile.readText().trim() else ""
                    }
                }
            }
        }
    }
}
