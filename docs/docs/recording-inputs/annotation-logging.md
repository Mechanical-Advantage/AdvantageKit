---
sidebar_position: 2
---

# Annotation Logging

By adding the `@AutoLog` annotation to your inputs class, AdvantageKit will automatically generate implementations of `toLog` and `fromLog` for your inputs. All simple data types (including single values and arrays) are supported. [Structured data types](DATA-FLOW.md#structured-data-types) and enum values are also supported, so geometry objects like `Rotation2d` and `Pose3d` can be directly used as inputs.

For example:

```java
@AutoLog
public class MyInputs {
    public double myNumber = 0.0;
    public Pose2d myPose = new Pose2d();
    public MyEnum myEnum = MyEnum.VALUE;
}
```

This will generate the following class:

```java
class MyInputsAutoLogged extends MyInputs implements LoggableInputs {
    public void toLog(LogTable table) {
        table.put("MyNumber", myField);
        table.put("MyPose", myPose);
        table.put("MyEnum", myEnum);
    }

    public void fromLog(LogTable table) {
        myNumber = table.get("MyNumber", myNumber);
        myPose = table.get("MyPose", myPose);
        myEnum = table.get("MyEnum", myEnum);
    }
}
```

Note that you should use the `<className>AutoLogged` class, rather than your annotated class. The [AdvantageKit examples projects](INSTALLATION.md#new-projects) are a useful reference for how to use `@AutoLog` in a full project.
