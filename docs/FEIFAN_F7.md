# Feifan F7 (飞凡F7) support

DiPlay focuses on BYD cars. This build adds a first-class **Feifan F7 preset** so that the
RISING OS head unit (Qualcomm SA8155, 15.05" 2.5K centre screen, 12.3" 1920x1080 passenger
screen) gets the same treatment: correct CarPlay density on the large centre screen and a
route to the passenger screen for the dashboard map.

> **Status: experimental.** No F7 has been measured by a DiPlay developer yet. Everything below
> follows [COMPATIBILITY.md](COMPATIBILITY.md): detection only *suggests* the preset, the driver
> confirms it, and every value that was derived from public specs instead of a measured car is
> marked as such.

## What the preset changes

Settings → Vehicle → **Feifan F7 preset (experimental)**:

| Change | Value | Source |
| --- | --- | --- |
| CarPlay physical width | 325 mm | 15.05" 16:10 panel ≈ 324 mm wide, snapped to the 5 mm grid (public spec, not measured) |
| Dashboard-map display | Accepts any car-sized presentation display (≥ 1280 px wide) | The F7's 12.3" 1920x1080 passenger screen is a presentation display on RISING OS (unverified) |

The preset has three states: **Automatic** (follows Build-based detection), **On** and **Off**.
Because no F7 has been measured, the Build identifiers may miss your car — if the status line
says "No Feifan head unit detected", turn the preset **On** manually. The physical width applies
at the next connection; a running CarPlay session asks before reconnecting.

Everything else (resolution, frame rate, aspect ratio, audio) already adapts automatically:

- The centre screen is announced to the iPhone at its real window size — 2.5K 16:10 works
  out of the box, no preset needed. Use Display → Resolution to scale down if you prefer.
- Audio uses usage-based routing by default on non-BYD units. If navigation prompts are silent
  or mix badly with music, try Audio → navigation channel grid (0–20, tap to preview).
- Steering-wheel keys: learn any key under Vehicle → Wheel keys (Siri key, play/pause,
  joystick). RISING OS key codes are not yet in the built-in tables.

## Installing on the car

Community-verified route (same as installing 布丁UI or custom 高德 on the F7):

1. Turn on the car's hotspot. The head unit is reachable at `192.168.43.1`.
2. Connect your phone to that hotspot and use a phone ADB helper (甲壳虫ADB助手) or a PC with
   `adb connect 192.168.43.1:5555` to install the DiPlay APK.
3. Open DiPlay on the centre screen, run the connection setup, then enable the Feifan F7 preset.

Note: this install-time ADB is from your phone. DiPlay's own optional extras (USB auto-confirm,
wheel-key accessibility service, boot start) use the head unit's local ADB (`127.0.0.1:5555`)
and only work while the unit's adbd listens on the network — on RISING OS that depends on the
engineering settings and is unverified.

## Wired USB does not detect the iPhone

When DiPlay keeps saying "The car doesn't see any USB device yet" / “车机尚未检测到任何 USB
设备”, the first question is whether anything reaches the kernel at all. Work through the list:

1. **Data cable, data port.** Charging-only cables and charge-only USB ports never enumerate a
   device. Try every port of the car (F7 has several) with a known data cable.
2. **Unlock the iPhone and approve Trust** when asked; retry after replugging.
3. **Check the car's own USB view.** If RISING OS's file manager or media app also does not
   react to a USB stick on the same port, that port carries no data.

DiPlay also records one evidence line per wired wait into the diagnostic report
(Settings → Diagnostics), behind the framework's back:

- `USB sysfs probe: kernel lists no Apple device either; kernel=[…] framework=[…]` — the kernel
  itself sees no iPhone: a port or cable problem, or the phone sits on a port wired to a charging
  chip. Only a different port/cable can fix this; no app can.
- `USB sysfs probe: kernel lists an Apple device the framework does not; kernel=[…] framework=[…]`
  — the kernel sees the iPhone but Android's `UsbManager` hides it from apps: a RISING OS
  framework restriction or a vendor daemon claiming the phone. Export the report and share it;
  this is the case a DiPlay update could address once the blocking service is identified.
- `USB sysfs probe unavailable (local ADB not approved or busy)` — inconclusive: the head unit's
  local ADB (`127.0.0.1:5555`) was not approved. Approve DiPlay's key once when prompted
  (engineering-mode ADB must be on), then replug the iPhone.

The wired wait never triggers an ADB approval prompt by itself; approve the key from the
ADB extras in Settings instead.

## Measured on a real F7 (2026-11-14, 0.2.25–0.2.27-r1)

- **Auto-detection hits**: Build fingerprint `risingauto/ep35/ep35:9/PQ3B.190801.002/...` contains
  `risingauto`, so the preset turned itself on and applied the 325 mm width (report shows
  "CarPlay size: Custom, 325 mm"). The exact product code `ep35` is also matched, so detection
  survives a rebranding.
- **Android 9 / API 28**, board `msmnile`, head unit `QTI ep35`. Android 7.1–9 features apply:
  the USB-permission ADB grant fallback and the Wi-Fi Direct legacy path.
- **Wired USB works**: "USB charging offer: normal (2400 mA)" is negotiated inside the wired
  iAP2 session — the iPhone enumerates, permission is granted and iAP2 handshakes.
- **Main screen: 2560x1600** (reported by the app), matching the preset's assumption.
- **Two presentation displays, both 1920x720** (`1:HDMI`, `2:HDMI`), plus a 1280x720 virtual —
  the dashboard map has real surfaces to target. Which is the instrument strip and which is the
  passenger screen still needs eyes on the car.
- **The F7's adbd listens on TCP 5555 persistently** (`persist.adb.tcp.port=5555`, confirmed
  with `netstat` including loopback), so DiPlay's own local-ADB extras (USB-permission grant,
  accessibility enable, VPN pre-consent, boot start) work without extra setup once DiPlay's ADB
  key is approved, and phone-side install reaches `192.168.43.1:5555` on the car hotspot.
- **Wireless CarPlay fully works** (0.2.27-r1 session, iPhone 14 Pro / iOS 27.2): main screen
  22–26 fps with hardware decode p50=51 ms, media (AAC) with zero underruns, navigation prompts
  (Opus) audible, and the **dashboard map running at the same time** (1600x600 on a 1920x720
  presentation display, 17–29 fps). The car also keeps its own cellular internet (vlan8).
- **Audio stutter over the car hotspot — root cause measured**: the hotspot runs on 5 GHz
  (channel 36), but RISING OS keeps a disconnected Wi-Fi client scanning beside it on the same
  radio. Field audio shows link stalls in a ~10 s rhythm with peaks to 1.9 s and packet loss
  (up to 72 frames). Since 0.2.29-r1 the BYD Wi-Fi Direct scan pause also runs for these
  hotspot sessions while the Feifan preset is on (needs DiPlay's ADB key approved once). A
  second field session still showed two rebuffers per 10 min on the raw link; for the strongest
  music fix also enable **Buffered music** (Settings → Display, experimental): Apple Music then
  arrives pre-buffered over TCP and short radio gaps are not heard at all.
- **Wired is blocked by firmware VPN consent**: the dialog opens and auto-denies within ~2 s,
  and unlike the Geely E8 case, `appops set … ACTIVATE_VPN allow` does not satisfy
  `VpnService.prepare()` on this Android 9 build — DiPlay's own ADB allowance runs (`access=
  READY`, key approved) but reports `granted=false`. An `always_on_vpn_app` secure setting is
  being tested as the remaining route; remove it with
  `settings delete secure always_on_vpn_app` if it misbehaves.
- **The one blocker in that report: "VPN 授权被拒绝"**. Wired CarPlay bridges the iPhone's USB
  network through an Android VPN service; Android asks for consent once with a system dialog.
  On the F7 the consent activity opens and answers "denied" within ~2 s — twice in a row,
  faster than a tap — so RISING OS appears to auto-deny it. Since 0.2.27-r1 a denied dialog
  also runs the ADB allowance and shows the command on the CarPlay screen; run it once from a
  phone connected to the car hotspot (`adb connect 192.168.43.1:5555`):
  `adb shell appops set com.shihab.diplay ACTIVATE_VPN allow`, then replug the iPhone.

## What is still unknown

These need reports from real cars before they can be turned on by default. Export a diagnostic
report (Settings → Diagnostics) after trying the feature and share it:

- Steering-wheel key codes (if keys do not reach the learn dialog, RISING OS routes them through
  a private input service like Geely does, and a small integration is needed).
- Whether navigation prompts have a dedicated audio channel on the F7.
- Which of the two 1920x720 presentation displays is which screen, and whether the cluster map
  lands on the intended one.

## 中文说明

DiPlay 本为比亚迪车机打造，本版本新增**飞凡F7预设**（设置 → 车辆 → 飞凡F7预设（实验性））：

- **15.05 英寸中控屏**：预设开启后 CarPlay 按 325 毫米物理宽度渲染，iOS 控件密度与大屏匹配
  （依据公开规格推算，未实车测量）。分辨率、帧率、宽高比本身已自动适配 2.5K 屏幕。
- **副驾屏**：预设开启后，仪表地图功能可发现任何车机级演示显示屏（副驾屏 1920x1080），
  未实测，失败不影响其它功能。
- **检测**：自动识别基于 Build 标识（feifan / risingauto / saic+rising）。目前没有实测数据，
  识别不到时请手动设为"开"。
- **安装**：开车机热点，手机连上后用 ADB 助手连接 `192.168.43.1:5555` 安装 APK（社区已验证
  的通用做法）。
- **USB 连不上（提示"车机尚未检测到任何 USB 设备"）**：按顺序排除——换数据线（很多线只能
  充电）、换车机的其它 USB 口（有些口只接充电芯片）、解锁 iPhone 并点"信任"。DiPlay 会在每次
  有线等待时通过车机本地 ADB 记一条内核层证据进诊断报告：
  - `kernel lists no Apple device either` → 内核也没看到 iPhone，是接口/线缆问题，应用层无法修复；
  - `kernel lists an Apple device the framework does not` → 内核看得到但系统框架对应用隐藏了
    iPhone（RISING OS 限制或厂商服务占用），请导出诊断报告反馈，这是后续版本能针对性处理的情况；
  - `probe unavailable` → 本地 ADB 未授权，结论不可用。
  另外本版本已让 USB 自动确认兼容 OEM 命名的 systemui 包（如 `com.rising.systemui`），
  弹窗出现时能自动点"允许"。
- **待实测**：车机 Build 标识、方向盘按键码、USB 弹窗包名、导航音频通道。请导出诊断报告
  （设置 → 诊断）反馈给社区，帮助把预设从"实验性"转为"已验证"。
