# DiPlay-R

> **English summary:** DiPlay-R is a community fork of [DiPlay](https://github.com/shihabal3amri/DiPlay) — an independent wired + wireless CarPlay receiver for Android head units — tuned for the **Feifan F7** (SAIC RISING OS, Qualcomm SA8155). It adds a Feifan F7 preset (325 mm CarPlay physical width for the 15.05-inch centre screen, passenger-screen dashboard-map discovery), wired-USB enumeration diagnostics that separate port/cable problems from framework-hidden devices, and USB auto-confirm support for OEM systemui packages. All upstream DiPlay 0.2.22 features (including BYD support) are kept. GPL-3.0 with full upstream attribution preserved. Feifan support is **experimental** and driven by community feedback — details below, in Chinese.

![DiPlay home](site/assets/home.png)

**装在车机上的 CarPlay 接收器**：iPhone 有线（USB）或无线（Wi-Fi）连接，在飞凡F7 的 15.05 英寸中控屏上使用 CarPlay。无需越狱、无需盒子、无需 Mac。基于 DiPlay 0.2.22。

## 与上游的关系

- 本仓库是 [shihabal3amri/DiPlay](https://github.com/shihabal3amri/DiPlay) 的社区分支，上游专注比亚迪（BYD DiLink）车机
- DiPlay 基于 [xcertplay](https://github.com/shilapi/xcertplay)（GPL-3.0），界面改编自 [DiAuto](https://github.com/shihabal3amri/DiAuto)（AGPL-3.0，许可文件见 `docs/licenses`），本 fork 保留全部上游署名与许可
- 上游不受理其它品牌问题——**飞凡/RISING OS 相关问题请到[本仓库 issues](https://github.com/xhg8124-a11y/Diplay-R/issues)反馈**，BYD 问题仍请去上游

## 支持状态（如实）

| 功能 | 状态 |
|---|---|
| 无线 CarPlay（Wi-Fi Direct / 车机热点 / 同一局域网） | ✅ **实车完整验证**：视频 22–26fps 硬解、媒体/导航音频、触摸（iPhone 14 Pro / iOS 27.2） |
| 有线 CarPlay（USB） | ⚠️ USB/iAP2 正常，被车机固件 VPN 授权自动拒绝拦截，见[排查指南](docs/FEIFAN_F7.md) |
| 飞凡F7预设（325mm 物理宽度渲染、密度适配大屏） | ✅ 实车验证：自动检测命中，325mm 生效 |
| 副驾屏仪表地图 | ✅ **实车工作**：1600x600 流渲染在 1920x720 演示屏，与主屏同时运行 |
| 方向盘按键（Siri/播放/摇杆，可学习任意键位） | 🔬 通用学习功能可用，F7 专属键码未收录 |
| 分辨率/帧率/图标大小、深色模式、诊断导出、11 种语言 | ✅ 沿用上游，开箱即用 |
| BYD 专属功能（HUD 导航等） | ✅ 沿用上游，与飞凡无关 |

## 安装（飞凡F7 / RISING OS）

1. **获取 APK**：从 [Releases](https://github.com/xhg8124-a11y/Diplay-R/releases/latest) 下载（应用内也支持自动更新提示）；测试构建可在 [Actions](https://github.com/xhg8124-a11y/Diplay-R/actions) 获取。GitHub 访问困难时，手机浏览器可用 `gh-proxy.com` 前缀代理下载；装好后在 **设置 → 关于 → 更新下载源** 选镜像，应用内检查与下载即走镜像线路。**如装有上游 DiPlay**：两者签名不同，请先卸载原版再安装；此后 Diplay-R 各版本间可直接覆盖更新
2. **开启车机热点**（设置 → 热点）
3. 手机连接车机热点，用 ADB 助手（如甲壳虫 ADB 助手）连接 `192.168.43.1:5555`，安装 APK——这是飞凡社区已验证的通用做法
4. 打开 DiPlay → 完成连接设置 → **设置 → 车辆 → 飞凡F7预设 → 开**（自动检测未命中时手动开启）
5. 解锁 iPhone，插线或按无线流程连接

## USB 连不上（提示“未检测到iPhone”）？

按顺序排除：换**数据线**（很多线只能充电）→ 换车机**其它 USB 口**（部分口只接充电芯片）→ 解锁 iPhone 并点“信任”。DiPlay 会在每次有线等待时记录一条内核层诊断进报告，能区分“接口/线缆问题”和“系统框架隐藏设备”两种根因——解读方法见 [飞凡F7适配指南](docs/FEIFAN_F7.md)。

## 从源码构建

```sh
./gradlew :mobile:assembleDebug
# 产物: mobile/build/outputs/apk/debug/mobile-debug.apk
```

要求 Android SDK、JDK 17，详见[构建文档](docs/BUILD.md)（主应用选 `mobile` 模块）。

## 反馈与诊断

1. 复现问题，**设置 → 诊断 → 保存诊断报告**（Android 10+ 通常存到 Downloads/DiPlay）
2. 检查 `.txt` 内容，删除热点密码等隐私信息
3. [新建 issue](https://github.com/xhg8124-a11y/Diplay-R/issues/new/choose)，附上报告并注明：车型/固件版本、iPhone/iOS 版本、USB 还是无线、相关设置、复现步骤

报告不会自动上传，仅在你主动分享时离开车机。你导出的诊断（Build 标识、USB 设备表、副驾屏信息）是把“实验性”变成“已验证”的关键——尤其欢迎。

## 文档

- [飞凡F7适配指南](docs/FEIFAN_F7.md)（预设说明、安装、USB 排查、待实测清单）· [繁體中文指南](README.zh-TW.md)
- [安装与连接](docs/INSTALL.md) · [兼容性与排查](docs/COMPATIBILITY.md) · [无线流畅度](docs/SMOOTH_WIRELESS.md)
- [隐私与诊断报告](docs/PRIVACY.md) · [从源码构建](docs/BUILD.md) · [上游 README 原文](docs/UPSTREAM-README.md)
- [发布说明](docs/RELEASE-NOTES-NEXT.md) · [历史版本](CHANGELOG.md)

## 声明与许可

- 本项目为 GPL-3.0 开源软件：基于 [xcertplay](https://github.com/shilapi/xcertplay)，界面改编自 [DiAuto](https://github.com/shihabal3amri/DiAuto)（AGPL-3.0），派生自 [DiPlay](https://github.com/shihabal3amri/DiPlay)。分发修改时须保留上述声明
- **非 Apple 认证产品**：APK 内置从公开 Carlinkit 固件提取的实验性配件身份，未来 iOS 是否继续接受无法保证；详见[上游 README](docs/UPSTREAM-README.md)
- CarPlay 及其图标归 Apple Inc. 所有；本项目与 Apple、上汽、飞凡均无隶属或背书关系
