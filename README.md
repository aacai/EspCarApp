# 🚗 EspCarClient

**用一套 Kotlin 代码，在 Android / iOS / Desktop / Web 上遥控你的 ESP32-C3 智能小车。**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20MP-1.11.1-4285F4?logo=jetpackcompose&logoColor=white)](https://github.com/JetBrains/compose-multiplatform)
[![KMP](https://img.shields.io/badge/Kotlin--Multiplatform-FF6A00?logo=kotlin&logoColor=white)](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](#license)

---

## ✨ 项目简介

**EspCarClient** 是 [ESP32-C3 小车](docs/协议文档.md) 的跨平台遥控客户端，基于 **Kotlin Multiplatform + Compose Multiplatform** 构建，
通过 **BLE（低功耗蓝牙，基于 [Kable](https://github.com/JuulLabs/kable)）** 与小车直连，并提供扫描设备、方向遥控、实时状态监控等能力，
同一套 UI 与业务逻辑运行在四大平台上。

> 一套代码，多端运行 —— Kotlin Multiplatform 与 Compose 的跨端实践项目。

### 核心特性

- 🔵 **BLE 直连**：基于 Kable 的跨平台蓝牙实现，无需路由器即可遥控
- 🕹️ **方向遥控**：方向键 / 触控，按下即动、松手即停（防失控）
- 📊 **实时状态面板**：动作、速度、指令数、上电时长、空闲内存、BLE/WiFi 连接、IP 一目了然
- 🔄 **自动重连**：记住上次连接设备，下次进入自动回连
- 🌐 **国际化 (i18n)**：内置「跟随系统 / 中文 / English」三态切换
- 🌗 **深浅色主题**：支持亮色 / 暗色一键切换
- 🖥️ **四端同 UI**：Android、iOS、Desktop (JVM)、Web (WasmJS) 共享全部界面与业务逻辑

---

## 🧱 技术栈

| 技术 | 版本 |
|------|------|
| Kotlin | 2.4.10 |
| Compose Multiplatform | 1.11.1 |
| Android Gradle Plugin | 9.0.1 |
| Android compileSdk / minSdk | 36 / 24 |
| Kable (BLE) | 0.44.3 |
| 目标平台 | Android、iOS (arm64 + simulatorArm64)、Desktop (JVM)、Web (WasmJS) |

---

## 🚀 快速开始

### 环境要求

- **JDK 21**（AGP 9 要求 17+，CI 统一使用 21）
- **Android Studio**（含 KMP 插件）
- **Xcode**（仅 iOS 编译需要，macOS）
- 一台支持 BLE 的 **ESP32-C3 小车**（固件见协议文档）

### 运行各平台

```bash
# Android
./gradlew :androidApp:assembleDebug

# Desktop
./gradlew :desktopApp:run
./gradlew :desktopApp:hotRun --auto      # 热重载

# Web (WasmJS)
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# iOS —— 在 Xcode 中打开 iosApp 目录运行
open iosApp/iosApp.xcodeproj
```

---

## 📦 打包构建（GitHub Actions）

`.github/workflows/build.yml` 提供各平台安装包的一键构建：

- **手动触发**：`Actions → Build Packages → Run workflow`，可勾选要构建的平台（Android / Desktop / Web / iOS）。
- **自动发布**：推送形如 `v1.0.0` 的 tag 时，全量构建并汇总发布到 GitHub Release。

各平台产出：

| 平台 | 产物 |
|------|------|
| Android | `release` APK + AAB（使用 AGP 自带 debug 签名，可直接安装） |
| Desktop | Windows `.msi` / `.exe`、macOS `.dmg`、Linux `.deb` |
| Web | WasmJS 静态文件（丢到任意静态服务器即可访问） |
| iOS | 模拟器 `.app`；在仓库 Secrets 中配置签名后导出真机 `.ipa` |

> iOS 真机 IPA 需在仓库 `Settings → Secrets` 配置 `IOS_CERTIFICATE_P12_BASE64`、`IOS_CERTIFICATE_PASSWORD`、`IOS_PROVISION_PROFILE_BASE64`、`IOS_TEAM_ID`、`IOS_SIGNING_IDENTITY`、`IOS_PROFILE_NAME`。

---

## 🗂️ 项目结构

```
EspCarClient/
├── androidApp/      # Android 入口
├── iosApp/          # iOS 入口（SwiftUI 桥接，内嵌 Kotlin 框架）
├── desktopApp/      # Desktop (JVM) 入口
├── webApp/          # Web (WasmJS) 入口
├── shared/          # 跨平台共享代码（核心）
│   └── src/commonMain/
│       ├── kotlin/zhiqiu/car/app/
│       │   ├── App.kt              # 根入口与页面路由
│       │   ├── ble/                # 控制层 + BLE 实现 + 协议解析
│       │   └── ui/                 # Compose 界面与主题
│       └── composeResources/       # 多语言字符串资源（values / values-zh）
└── docs/
    └── 协议文档.md   # ESP32-C3 小车通信协议
```

---

## 📄 License

本项目以 **MIT License** 开源。详见 [LICENSE](LICENSE) 文件。

---

<div align="center">
Made with ❤️ and Kotlin Multiplatform
</div>
