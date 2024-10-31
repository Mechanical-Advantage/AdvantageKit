---
sidebar_position: 2
---

# Mechanism2d

AdvantageKit can also log [`Mechanism2d`](https://docs.wpilib.org/en/stable/docs/software/dashboards/glass/mech2d-widget.html) objects as outputs, which can be viewed using AdvantageScope. If not using `@AutoLogOutput`, note that the logging call only records the current state of the `Mechanism2d` and so it must be called periodically.

```java
public class Example {
    @AutoLogOutput // Auto logged as "Example/Mechanism"
    private Mechanism2d mechanism = new Mechanism2d(3, 3);

    public void periodic() {
        // Alternative approach if not using @AutoLogOutput
        // (Must be called periodically)
        Logger.recordOutput("Example/Mechanism", mechanism);
    }
}
```
