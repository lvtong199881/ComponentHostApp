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

// 读取模块路径配置
val modulesFile = file("modules.conf")
val modulePaths = mutableMapOf<String, String>()

if (modulesFile.exists()) {
    modulesFile.readLines()
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .forEach { line ->
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) {
                modulePaths[parts[0].trim()] = parts[1].trim()
            }
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
