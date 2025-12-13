---
sidebar_position: 1
title: Welcome
slug: /
---

import DocCardList from "@theme/DocCardList";

#

![AdvantageKit](./img/banner.png)

AdvantageKit is a logging, telemetry, and replay framework developed by [Team 6328](https://littletonrobotics.org). AdvantageKit enables **deterministic log replay** in Java, allowing the full state of WPILib robot code to be efficiently replayed in simulation based on a log file ([What is AdvantageKit?](/getting-started/what-is-advantagekit)). AdvantageKit is [free and open-source](/legal/open-source-license) with no restrictions on vendor compatibility. See also:

- **[AdvantageScope](https://docs.advantagescope.org)**, Team 6328's robot telemetry application which **does not require AdvantageKit to use**.
- **[WPILib Data Logging](https://docs.wpilib.org/en/stable/docs/software/telemetry/datalog.html)**, a simpler logging system included in WPILib (does not support log replay in simulation, but covers the needs of most teams).
- **[PyKit](https://github.com/1757WestwoodRobotics/PyKit)**, a deterministic logging and replay framework for Python robot projects developed by [Team 1757](https://whsrobotics.org).

<DocCardList
items={[
{
type: "category",
label: "👋 What is AdvantageKit?",
href: "/getting-started/what-is-advantagekit"
},
{
type: "category",
label: "📒 API Documentation",
href: "pathname:///javadoc"
},
{
type: "category",
label: "📦 Installation",
href: "/getting-started/installation"
},
{
type: "category",
label: "🏠 Template Projects",
href: "/getting-started/template-projects"
},
{
type: "category",
label: "🔽 Recording Inputs",
href: "/data-flow/recording-inputs"
},
{
type: "category",
label: "🔼 Recording Outputs",
href: "/data-flow/recording-outputs/"
},
{
type: "category",
label: "⚠️ Common Issues",
href: "/getting-started/common-issues"
},
{
type: "category",
label: "⚙️ SysId Compatibility",
href: "/data-flow/sysid-compatibility"
}
]}
/>

Feedback, feature requests, and bug reports are welcome on the [issues page](https://github.com/Mechanical-Advantage/AdvantageKit/issues). For non-public inquires, please send a message to software@team6328.org.
