---
sidebar_position: 3
---

# Uninitialized Inputs

Typically, inputs from subsystems are only updated during calls to `periodic`. Note that this means updated (non-default) input data is not available in the constructor. The solution is to either wait for the first `periodic` call or call `periodic` from within the constructor.

```java
public class Example extends SubsystemBase {
    private final ExampleIO io;
    private final ExampleIOInputs inputs = new ExampleIOInputs();

    public Example(ExampleIO io) {
        this.io = io;

        // Inputs are not updated yet
        inputs.position;
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        Logger.processInputs("Example", inputs);

        // Inputs are now updated
        inputs.position;
    }
}
```
