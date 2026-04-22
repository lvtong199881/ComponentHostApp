# ComponentHostApp

Android 组件化宿主壳工程，用于加载 [AndroidComponentProject](https://github.com/lvtong199881/AndroidComponentApp) 中的各个组件模块。

---

## 🎯 项目特点

- **源码/远程依赖切换** - 通过 `modules.json` 配置，可自由切换源码依赖和 Maven 依赖
- **模块独立管理** - 每个组件可独立仓库开发，通过 `modules.json` 指定路径
- **自动检测机制** - 检测 `modules.json` 中配置的模块路径是否存在，自动选择依赖模式

---

## 📁 项目结构

```
ComponentHostApp/
├── app/                           # 宿主应用模块
├── modules.json                    # 模块路径配置
├── settings.gradle.kts             # 自动检测并配置源码/Maven依赖
└── dependencies-config.gradle.kts # Maven 依赖配置
```

---

## 🚀 快速开始

**环境要求：** Java 17 / Android Gradle Plugin 8.2.0 / Kotlin / Android SDK 34

```bash
./gradlew assembleDebug
```

---

## 🔧 模块配置

在 `modules.json` 中配置各组件模块的路径：

```json
{
  "base": "../AndroidComponentProject/base",
  "router": "../AndroidComponentProject/router",
  "network": "../AndroidComponentProject/network",
  "login": "../AndroidComponentProject/login",
  "home": "../AndroidComponentProject/home",
  "user": "../AndroidComponentProject/user",
  "reactnative": "../AndroidComponentProject/reactnative",
  "logger": "../AndroidComponentProject/logger",
  "websdk": "../AndroidComponentProject/websdk"
}
```

### 路径说明

| 配置值 | 说明 |
|--------|------|
| 本地路径 | 相对路径或绝对路径，如 `../AndroidComponentProject/base` |
| 远程仓库 | 未来可将模块独立成仓库，配置对应路径即可 |

---

## 📦 依赖模式

### 源码依赖模式

当 `modules.json` 中配置的路径存在时，自动使用源码依赖：
- 修改源码即时生效
- 支持断点调试
- 用于开发调试阶段

### Maven 依赖模式

当 `modules.json` 中配置的路径不存在时，自动切换到 Maven 依赖：
- 从 GitHub Packages 下载组件
- 构建速度快
- 用于发布或 CI/CD

---

## 📦 组件依赖

组件来自 [AndroidComponentProject](https://github.com/lvtong199881/AndroidComponentApp)，已发布到 GitHub Packages：

```kotlin
dependencies {
    implementation("com.mohanlv.component:startup:1.0.4")
    implementation("com.mohanlv.component:router:1.0.4")
    implementation("com.mohanlv.component:network:1.0.4")
    implementation("com.mohanlv.component:base:1.0.4")
    implementation("com.mohanlv.component:login:1.0.4")
    implementation("com.mohanlv.component:home:1.0.4")
    implementation("com.mohanlv.component:user:1.0.4")
    implementation("com.mohanlv.component:reactnative:1.0.4")
    implementation("com.mohanlv.component:logger:1.0.4")
    implementation("com.mohanlv.component:websdk:1.0.4")
}
```

---

## 📄 License

MIT License

---

**🌙 莫寒慕 · lvtong199881**
