---
sidebar_position: 2
---

# Annotation Logging

By adding the `@AutoLog` annotation to your inputs class, AdvantageKit will automatically generate implementations of `toLog` and `fromLog` for your inputs. All [data types](../supported-types.md) are supported with the exception of mechanism states. Loggable inputs can also be nested and used as fields.

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

Note that you should use the `<className>AutoLogged` class, rather than your annotated class. The [AdvantageKit template projects](/getting-started/template-projects) are a useful reference for how to use `@AutoLog` in a full project.

## Units

To ensure that AdvantageScope will correctly [visualize unit data](https://docs.advantagescope.org/tab-reference/line-graph/units), units can be specified by modifying the field name or using a `Measure` object.

:::info
Unlike when logged as [outputs](/data-flow/recording-outputs/#units), `Measure` values in an inputs class are always logged using the **base unit** and not the user-specified unit (e.g. distances will always be logged in meters). This ensures that unit differences between IO implementations are not reflected in the logged data.
:::

```java
@AutoLog
public class MyInputs {
    public double myDistanceMeters = 0.0;
    public Distance myDistance = Meters.of(0.0);
}
```
