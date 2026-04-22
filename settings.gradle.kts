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

// 检测是否有任何模块配置了源码路径
val hasSourceModules = modulePaths.values.any { path ->
    file(path).exists()
}

if (hasSourceModules) {
    println("检测到源码模块配置，使用源码依赖模式")

    // 为每个配置了路径的模块创建 includeBuild
    modulePaths.forEach { (moduleName, modulePath) ->
        if (file(modulePath).exists()) {
            println("  - $moduleName: $modulePath")
            includeBuild(modulePath) {
                dependencySubstitution {
                    substitute(module("com.mohanlv.component:$moduleName")).using(project(":$moduleName"))
                }
            }
        }
    }
} else {
    println("未检测到源码模块，使用 Maven 依赖模式")
}

// 应用组件依赖配置
apply(from = "dependencies-config.gradle.kts")
