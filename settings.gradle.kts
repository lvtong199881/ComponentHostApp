pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "ComponentHostApp"

// 宿主应用模块
include(":app")

// 读取模块路径配置（JSON格式）
val modulesFile = file("modules.json")
val modulePaths = mutableMapOf<String, String>()

if (modulesFile.exists()) {
    try {
        val json = groovy.json.JsonSlurper().parse(modulesFile) as Map<*, *>
        json.forEach { (key, value) ->
            if (key is String && value is String) {
                modulePaths[key] = value
            }
        }
    } catch (e: Exception) {
        println("Warning: Failed to parse modules.json: ${e.message}")
    }
}

// 检测是否有任何源码模块配置
val hasSourceModules = modulePaths.values.any { path ->
    file(path).exists()
}

if (hasSourceModules) {
    println("检测到源码模块配置，使用源码依赖模式")
    
    // 找出所有模块所在的 settings.gradle.kts 目录
    val settingsDir = modulePaths
        .filter { (_, path) -> file(path).exists() }
        .map { (_, path) -> file(path).parentFile.absolutePath }
        .firstOrNull { dir -> file("$dir/settings.gradle.kts").exists() }
    
    if (settingsDir != null) {
        println("  - 加载源码项目: $settingsDir")
        
        // 配置 includeBuild 和依赖替换
        includeBuild(settingsDir) {
            dependencySubstitution {
                modulePaths.forEach { (moduleName, modulePath) ->
                    if (file(modulePath).exists()) {
                        substitute(module("com.mohanlv.component:$moduleName")).using(project(":$moduleName"))
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
