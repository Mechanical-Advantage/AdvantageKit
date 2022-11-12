# Code Structure & IO Layers

## `@AutoLog` Annotation

As of version 1.8, a new `@AutoLog` annotation was added. By adding this annotation to your inputs class, AdvantageKit will automatically generate implementations of `toLog` and `fromLog` for your inputs.

For example:

```java
@AutoLog
public class MyInputs {
    public double myField = 0;
}
```

This will generate the following class:

```java
class MyInputsAutoLogged extends MyInputs implements LoggableInputs {
    public void toLog(LogTable table) {
        table.put("MyField", myField);
    }

    public void fromLog(LogTable table) {
        myField = table.getDouble("MyField", myField);
    }
}
```

Note that you should use the `<className>AutoLogged` class, rather than your annotated class.
