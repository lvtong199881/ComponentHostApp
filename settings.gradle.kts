pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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
                password = System.getenv("GITHUB_TOKEN")
                    ?.takeIf { it.isNotBlank() }
                    ?: run {
                        val tokenFile = java.io.File(System.getProperty("user.home"), ".github_token")
                        if (tokenFile.exists()) tokenFile.readText().trim() else ""
                    }
                    ?.takeIf { it.isNotBlank() }
                    ?: run {
                        val gradleProps = java.util.Properties()
                        val gradleFile = java.io.File(System.getProperty("user.home"), ".gradle/gradle.properties")
                        if (gradleFile.exists()) {
                            gradleFile.inputStream().use { gradleProps.load(it) }
                        }
                        gradleProps.getProperty("gpr.token", "")
                    }
            }
        }

    }
}

rootProject.name = "ComponentHostApp"

// 宿主应用模块
include(":app")

// 读取模块路径配置（HOCON 格式，支持 # 注释）
val modulesFile = file("modules.conf")
val modulePaths = mutableMapOf<String, String>()

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

// 找出包含 settings.gradle.kts 的目录（模块的父项目）
fun findSettingsDir(modulePath: String): String? {
    var dir = file(modulePath)
    while (dir.parentFile != null) {
        dir = dir.parentFile
        if (file("${dir.absolutePath}/settings.gradle.kts").exists()) {
            return dir.absolutePath
        }
    }
    return null
}

// 收集所有需要 includeBuild 的项目（去重）
val settingsDirs = mutableSetOf<String>()
modulePaths.forEach { (_, modulePath) ->
    if (file(modulePath).exists()) {
        val settingsDir = findSettingsDir(modulePath)
        if (settingsDir != null) {
            settingsDirs.add(settingsDir)
        }
    }
}

// 执行 includeBuild
if (settingsDirs.isNotEmpty()) {
    println("检测到源码模块配置，使用源码依赖模式")
    settingsDirs.forEach { dir ->
        println("  - 源码项目: $dir")
    }
    
    // 对每个包含 settings.gradle.kts 的项目执行一次 includeBuild
    settingsDirs.forEach { settingsDir ->
        includeBuild(settingsDir) {
            dependencySubstitution {
                modulePaths.forEach { (moduleName, modulePath) ->
                    if (file(modulePath).exists() && findSettingsDir(modulePath) == settingsDir) {
                        substitute(module("com.mohanlv:$moduleName")).using(project(":$moduleName"))
                    }
                }
            }
        }
    }
} else {
    println("未检测到源码模块，使用 Maven 依赖模式")
}

// 应用组件依赖配置
apply(from = "dependencies-config.gradle.kts")
