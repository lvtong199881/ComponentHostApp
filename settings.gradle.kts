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

// 源码依赖模式：检查 ../Components 目录是否存在
val componentsDir = file("../Components")
if (componentsDir.exists()) {
    println("📦 检测到 Components 目录，使用源码依赖模式")
    includeBuild("../Components") {
        dependencySubstitution {
            substitute(module("com.mohanlv.component:base")).using(project(":base"))
            substitute(module("com.mohanlv.component:router")).using(project(":router"))
            substitute(module("com.mohanlv.component:network")).using(project(":network"))
            substitute(module("com.mohanlv.component:login")).using(project(":login"))
            substitute(module("com.mohanlv.component:home")).using(project(":home"))
            substitute(module("com.mohanlv.component:user")).using(project(":user"))
            substitute(module("com.mohanlv.component:reactnative")).using(project(":reactnative"))
            substitute(module("com.mohanlv.component:logger")).using(project(":logger"))
            substitute(module("com.mohanlv.component:websdk")).using(project(":websdk"))
        }
    }
} else {
    println("📦 未检测到 Components 目录，使用 Maven 依赖模式")
}

// 应用组件依赖配置
apply(from = "dependencies-config.gradle.kts")
