---
sidebar_position: 2
---

# Mechanism2d

AdvantageKit can also log 2D mechanism objects as outputs, which can be viewed using AdvantageScope. If not using `@AutoLogOutput`, note that the logging call only records the current state of the `Mechanism2d` and so it must be called periodically.

:::warning
Mechanism objects must use the **`LoggedMechanism2d`** class to be compatible with AdvantageKit. This class is otherwise equivalent to the standard `Mechanism2d` class. Equivalent `LoggedMechanismRoot2d`, `LoggedMechanismObject2d`, and `LoggedMechanismLigament2d` classes are also provided.
:::

```java
public class Example {
    @AutoLogOutput // Auto logged as "Example/Mechanism"
    private LoggedMechanism2d mechanism = new LoggedMechanism2d(3, 3);

    public void periodic() {
        // Alternative approach if not using @AutoLogOutput
        // (Must be called periodically)
        Logger.recordOutput("Example/Mechanism", mechanism);
    }
}
```
