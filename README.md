# 专注桌面 (Focus Launcher)

Android 专注启动器。把它设为默认桌面后，主屏幕只显示**本次会话允许**的应用。修改名单或退出专注桌面，需要强生物识别或设备锁屏凭据。重启后会话作废，必须重新选择。

包名 `com.focuslauncher.app`，版本 `1.0.0`，最低 Android 9（API 28）。

**Focus Launcher** is a home-screen replacement. The grid shows only the apps chosen for the current session. Reboot clears that session. Phone and Settings stay available as escape hatches. It does not lock the device.

## 安装

从 [v1.0.0 Release](https://github.com/oerjc/focus-launcher/releases/tag/v1.0.0) 下载 `FocusLauncher-debug.apk`。这是 debug 签名包，可直接安装；若系统拦截未知来源，允许该来源后再装。

安装后打开「专注桌面」，或在系统设置里把它选为默认主屏幕应用。

## 设为默认桌面

应用注册为 `HOME`。当前不是默认桌面时，主界面会显示提示和「设为默认桌面」，打开系统的默认主屏幕设置。该设置页打不开时，改为弹出桌面选择器。

只有设成默认主屏幕之后，按 Home 才会回到专注桌面。

## 本次会话允许名单

没有有效会话时，进入「今天要打开哪些应用？」。列表来自本机带启动器入口的应用（不含专注桌面自己），可按名称或包名搜索。至少勾选一项并确认后，名单写入本次会话。

桌面以四列网格显示这些应用，每个包名只保留一个图标。会话中途要改名单，点「解锁后修改名单」，验证通过后重选；已选项会预先勾上。尚未建立会话时，返回键留在选择页，避免空名单直接进入桌面。

## 生物识别与设备凭据

「解锁后修改名单」和「解锁后退出专注桌面」使用系统 `BiometricPrompt`，接受：

- 强生物识别（`BIOMETRIC_STRONG`）
- 设备凭据（`DEVICE_CREDENTIAL`：锁屏 PIN、图案或密码）

设备上还没有可用的锁屏凭据或生物识别时，这两项会失败，并提示先到系统设置里配置。取消验证不会改名单，也不会退出。

底部的电话和设置不经过这道验证。

## 重启清空会话

会话保存在本应用的 `SharedPreferences` 里，并用两处逻辑在开机后失效：

1. `BootReceiver` 收到 `BOOT_COMPLETED` 后清空会话。
2. 建立会话时记下 `SystemClock.elapsedRealtime()`。重启后这个开机计时从零附近重新开始；若当前值小于保存值，会话视为已失效并清除。

因此每次开机都要重新选择今日应用。进程被系统杀掉但没有重启时，名单仍然有效。会话不按时钟时间自动过期。

## 电话与设置

主界面底部「应急」始终提供两个出口，不占用允许名单，也不需要解锁：

- **电话**：打开系统拨号界面（`ACTION_DIAL`），只进入拨号盘，不会直接呼出。
- **设置**：打开系统设置。可在其中更换默认桌面或管理应用。

「解锁后退出专注桌面」在验证通过后，打开默认主屏幕设置（失败则打开桌面选择器），用来换回原来的桌面。它不会卸载本应用。在默认桌面被改掉之前，按 Home 仍会回到这里。

## 能力边界

专注桌面替换的是主屏幕入口，不是设备主人模式、工作资料或屏幕固定。

- 通知、最近任务、快捷设置、分享、深链，以及已经在运行的应用，仍可能打开不在名单中的应用。
- 没有小部件、文件夹、壁纸、手势导航定制，也没有包含全部应用的抽屉。搜索只在选择名单时可用。
- 只列出声明了 `MAIN` / `LAUNCHER` 的活动，并依赖 `QUERY_ALL_PACKAGES` 枚举已安装应用。同一包名的多个启动入口合并成一个图标。
- 随 Release 提供的 APK 是 debug 构建（`versionName` 1.0.0，未混淆），不是应用商店用的 release 签名包。

## 从源码编译

需要 JDK 17 和 Android SDK（`compileSdk` 34）。

```bash
./gradlew assembleDebug
```

产物路径：`app/build/outputs/apk/debug/app-debug.apk`。

## 许可证

本项目以 [MIT License](LICENSE) 发布。Copyright (c) 2026 oerjc。
