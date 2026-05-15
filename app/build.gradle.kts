import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mohanlv.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mohanlv.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.findProperty("RELEASE_KEYSTORE_FILE") as String)
            storePassword = project.findProperty("RELEASE_KEYSTORE_PASSWORD") as String
            keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String
            keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { viewBinding = true }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }

    packaging {
        jniLibs {
            keepDebugSymbols += listOf("**/*.so")
        }
    }
}

// ============================================================
// 图片压缩配置
// - compressImages=false 禁用
// - compressImages.debug=true 为 debug 开启
// ============================================================
val compressEnabled = project.findProperty("compressImages") != "false"
val compressForDebug = project.findProperty("compressImages.debug") == "true"

// 图片压缩任务（不修改源文件）
val compressImages by tasks.registering {
    description = "压缩图片到 build/compressed_res（不修改源文件）"
    
    onlyIf { compressEnabled }
    
    doLast {
        val resDir = file("src/main/res")
        val outputDir = project.layout.buildDirectory.get().asFile.resolve("compressed_res")
        
        if (!resDir.exists()) {
            logger.info("res 目录不存在")
            return@doLast
        }
        
        outputDir.deleteRecursively()
        outputDir.mkdirs()
        
        var totalOriginal = 0L
        var totalCompressed = 0L
        var processed = 0
        var saved = 0L
        
        val extensions = listOf("png", "jpg", "jpeg", "webp")
        
        resDir.walk()
            .filter { it.isFile && it.extension.lowercase() in extensions }
            .forEach { file ->
                val originalSize = file.length()
                totalOriginal += originalSize
                
                val relativePath = file.relativeTo(resDir)
                val outputFile = File(outputDir, relativePath.path)
                outputFile.parentFile?.mkdirs()
                
                val compressedSize = compressImageWithPIL(file, outputFile)
                
                if (compressedSize != null && compressedSize < originalSize) {
                    totalCompressed += compressedSize
                    saved += originalSize - compressedSize
                    logger.info("✓${relativePath.path}|${formatSize(originalSize)}→${formatSize(compressedSize)}")
                } else {
                    file.copyTo(outputFile, overwrite = true)
                    totalCompressed += originalSize
                }
                processed++
            }
        
        val percent = if (totalOriginal > 0) saved * 100 / totalOriginal else 0
        println("[图片压缩] $processed 文件 | ${formatSize(totalOriginal)}→${formatSize(totalCompressed)} | 节省 ${formatSize(saved)}($percent%)")
    }
}

// variant 特定任务
android.applicationVariants.all {
    val variant = this
    val variantName = variant.name.replaceFirstChar { it.uppercase() }
    val isRelease = variant.buildType.name == "release"
    val shouldCompress = isRelease || compressForDebug
    
    tasks.register<DefaultTask>("compress${variantName}") {
        description = "compressImages 的依赖任务"
        group = "images"
        
        onlyIf { compressEnabled && shouldCompress }
        dependsOn("compressImages")
    }
    
    if (shouldCompress && compressEnabled) {
        tasks.getByName("assemble${variantName}").dependsOn("compress${variantName}")
    }
}

// PIL 图片压缩
fun compressImageWithPIL(input: File, output: File): Long? {
    val script = """
from PIL import Image
import sys

img = Image.open('${input.absolutePath.replace("'", "'\"'\"'")}')

if img.mode == 'RGBA':
    bg = Image.new('RGB', img.size, (255, 255, 255))
    bg.paste(img, mask=img.split()[3])
    img = bg
elif img.mode != 'RGB':
    img = img.convert('RGB')

buf = __import__('io').BytesIO()
ext = '${input.extension.lowercase()}'

if ext == 'png':
    img = img.quantize(colors=256)
    img.save(buf, format='PNG', optimize=True)
elif ext in ['jpg', 'jpeg']:
    img.save(buf, format='JPEG', quality=85, optimize=True)
elif ext == 'webp':
    img.save(buf, format='WEBP', quality=85, method=6)

sys.stdout.buffer.write(buf.getvalue())
"""
    
    val process = Runtime.getRuntime().exec(arrayOf("python3", "-c", script))
    process.outputStream.close()
    
    val compressed = process.inputStream.readBytes()
    val exitCode = process.waitFor()
    
    if (exitCode != 0 || compressed.isEmpty() || compressed.size >= input.length()) return null
    
    output.parentFile?.mkdirs()
    output.writeBytes(compressed)
    return compressed.size.toLong()
}

fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}

dependencies {
    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ========== 基础组件 (Maven) ==========
    implementation("com.mohanlv:base:1.2.34")
    implementation("com.mohanlv:startup:1.2.35")
    implementation("com.mohanlv:router:1.2.16")
    implementation("com.mohanlv:network:1.2.21")
    implementation("com.mohanlv:logger:1.2.32")

    // ========== 业务组件 (Maven) ==========
    implementation("com.mohanlv:common:1.0.19")
    implementation("com.mohanlv:login:1.2.22")
    implementation("com.mohanlv:home:1.2.29")
    implementation("com.mohanlv:user:1.2.25")
    implementation("com.mohanlv:reactnative:1.2.14")
    implementation("com.mohanlv:websdk:1.2.21")
    implementation("com.mohanlv:shortvideo:1.2.32")
    // ===================================
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    
    // React Native
    implementation("com.facebook.soloader:soloader:0.11.0")
    implementation("com.facebook.react:react-android:0.76.9")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}