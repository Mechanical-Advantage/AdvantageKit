---
title: What's New in 2027?
sidebar_position: 2
---

import BannerLight from "./img/whats-new/banner-light.png";
import BannerDark from "./img/whats-new/banner-dark.png";
import MechanismImg from "./img/whats-new/mechanism.png";

#

<img src={BannerLight} className="light-only" />
<img src={BannerDark} className="dark-only" />

The 2027 version of AdvantageKit is now available! Check the [installation docs](/getting-started/installation/) and [full changelog](https://github.com/Mechanical-Advantage/AdvantageKit/releases) for details. In addition to adding support for WPILib 2027 and Systemcore, this release includes several major new features and enhancements. The most notable features in this release are documented below.

**We value your feedback! Feedback, feature requests, and bug reports are welcome on the [issues page](https://github.com/Mechanical-Advantage/AdvantageKit/issues).**

## 🍋‍🟩 Systemcore Compatibility {#systemcore-compatibility}

- New system stats (IMU, CAN status, network status, faults, etc)
  - Unit metadata for all built-in fields
- WPILib 2027 compatibility

## 🕹️ OpMode Integration {#opmode-integration}

## 🏠 2027 Template Projects {#updated-template-projects}

## 🪵 WPILOGXZ Compression {#wpilogxz-compression}

## 🪐 Vendor Log Capture {#vendor-log-capture}

## 🚨 Built-In Logger Alerts {#built-in-logger-alerts}

## 🎛️ Performance Management {#performance-management}

- Improved log receiver diagnostics
- Configurable loop overrun warning
- Wait for log receivers during replay

## 🧱 Platform Compatibility {#platform-compatibility}

- Build with Java 25
- Add support for Windows ARM64
- Native binaries signed on Windows & macOS
