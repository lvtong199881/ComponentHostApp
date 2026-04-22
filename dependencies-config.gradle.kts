// 组件依赖配置脚本
// ComponentHostApp 作为壳工程，通过 Maven 依赖Components

val componentVersion = providers.gradleProperty("component.version").orNull ?: "1.0.0"
val componentGroupId = providers.gradleProperty("component.groupId").orNull ?: "com.mohanlv.component"

println("========================================")
println("📦 ComponentHostApp - Maven Dependency Mode")
println("   Version: $componentVersion")
println("   GroupId: $componentGroupId")
println("========================================")

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
