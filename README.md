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
│       │       ├── control/        # 控制页（包）
│       │       │   ├── ControlScreen.kt
│       │       │   └── components/ # 控制页专属组件
│       │       │       ├── DirectionalPad.kt
│       │       │       └── StatusPanel.kt
│       │       ├── scan/           # 扫描页（包）
│       │       │   └── ScanScreen.kt
│       │       ├── unsupported/    # 蓝牙不可用提示页（包）
│       │       │   └── UnsupportedScreen.kt
│       │       ├── components/     # 通用组件（跨页面复用）
│       │       │   ├── CarIcons.kt
│       │       │   └── TopBarToggles.kt
│       │       └── theme/          # 主题与配色
│       └── composeResources/       # 多语言字符串资源（values / values-zh）
└── docs/
    └── 协议文档.md   # ESP32-C3 小车通信协议
```

---

## 🔧 关键实现

### 架构：一套代码，四端共享
业务逻辑与界面全部位于 `shared` 模块的 `commonMain`，平台相关能力通过 Kotlin 的 `expect/actual` 下沉到各端：
- **BLE 通信**：`ble/PlatformBle.kt` 声明期望，`androidMain / iosMain / jvmMain / wasmJsMain` 各自实现。
- **持久化存储**：`ble/KeyValueStore.kt` 声明 `expect fun createKeyValueStore()`，四端分别落地（见下）。

### BLE 与协议
- 基于 [Kable](https://github.com/JuulLabs/kable) 实现跨平台 BLE。
- 控制指令为单字符 ASCII（`F/B/L/R/S` 方向 + `0-9` 速度档位，由 `ble/protocol.kt` 的 `CarCommands` 编解码），固件大小写等价。
- 状态读取采用「通知触发 + 主动 Read 全量」以规避 MTU 分片；当 Notify 因 `scan` 数组超 BLE ATT 上限被截断时，由 `parseCarStatusTolerant` 扫描到顶层最后一个完整键值对补 `}` 再解析，仅丢弃非关键字段。

### 安全兜底（防失控）
`CarController` 内置多层保护，确保小车「绝不乱跑」：
- **松手即停**：抬起方向键立即发 `S`。
- **保活重发**：按住期间每 `KEEPALIVE_MS` 重发当前指令，防止丢包导致失控。
- **看门狗**：若超过 `WATCHDOG_TIMEOUT_MS` 未收到新指令却仍在运动，自动补发 `S`。
- **断连复位 + 自动重连**：断连即复位状态；状态读取连续失败（多为系统蓝牙栈清掉 GATT 上下文）时触发扫描重连。

### 持久化
自研极简键值存储抽象 `KeyValueStore`（零第三方依赖），四端分别落地：

| 平台 | 实现 |
|------|------|
| Android | `SharedPreferences` |
| iOS | `NSUserDefaults` |
| Desktop (JVM) | `java.util.prefs.Preferences` |
| Web (Wasm) | 浏览器 `localStorage` |

用于记住上次连接设备、看门狗 / 自动重连开关、深 / 浅色主题与语言。

### 状态收集
Compose UI 统一使用 `collectAsStateWithLifecycle()` 订阅 `StateFlow`，非 Android 平台自动回退，四端行为一致。

---

## 📄 License

本项目以 **MIT License** 开源。详见 [LICENSE](LICENSE) 文件。

---

<div align="center">
Made with ❤️ and Kotlin Multiplatform
</div>
