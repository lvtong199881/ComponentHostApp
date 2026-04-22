// 组件依赖配置脚本
// 源码依赖模式：Components 目录存在时自动启用
// Maven 依赖模式：从 GitHub Packages 获取组件

val componentVersion = providers.gradleProperty("component.version").orNull ?: "1.0.0"
val componentGroupId = "com.mohanlv.component"

// 检测源码依赖模式（Components 目录存在）
val useSourceDependency = file("../Components").exists()
val modeName = if (useSourceDependency) "SOURCE (Components/)" else "MAVEN (GitHub Packages)"

println("========================================")
println("ComponentHostApp Dependency Mode")
println("   Mode: $modeName")
println("   Version: $componentVersion")
println("   GroupId: $componentGroupId")
println("========================================")

if (!useSourceDependency) {
    // Maven 依赖模式（main 分支 / 发布）
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
        repositories {
            google()
            mavenCentral()
            mavenLocal()
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/lvtong199881/AndroidComponentApp")
                credentials {
                    username = "lvtong199881"
                    password = System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }
    }
}
