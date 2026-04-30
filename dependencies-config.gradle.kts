// 组件依赖配置脚本
// 从 modules.conf 读取源码模块路径（HOCON 格式，支持注释）

val modulePaths = mutableMapOf<String, String>()

val modulesFile = file("modules.conf")
if (modulesFile.exists()) {
    try {
        // 读取文件内容，移除 # 注释后当作 JSON 解析
        val content = modulesFile.readText()
        val withoutComments = content
            .split("\n")
            .map { line ->
                val idx = line.indexOf('#')
                if (idx >= 0) line.substring(0, idx) else line
            }
            .joinToString("\n")

        val json = groovy.json.JsonSlurper().parseText(withoutComments) as Map<*, *>
        json.forEach { (key, value) ->
            if (key is String && value is String) {
                modulePaths[key] = value
            }
        }
    } catch (e: Exception) {
        println("Warning: Failed to parse modules.conf: ${e.message}")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/lvtong199881/PackagesMaven")
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
