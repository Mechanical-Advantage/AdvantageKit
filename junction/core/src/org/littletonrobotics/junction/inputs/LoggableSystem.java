package org.littletonrobotics.junction.inputs;

import org.littletonrobotics.junction.Logger;

public class LoggableSystem<T extends LoggableIO<R>, R extends LoggableInputs> {
    private final T io;
    private final R inputs;
    private final String key;

    public LoggableSystem(T io, R inputs) {
        this.io = io;
        this.inputs = inputs;
        this.key = getClass().getSimpleName() + "Inputs";
    }

    public LoggableSystem(T io, R inputs, String key) {
        this.io = io;
        this.inputs = inputs;
        this.key = key;
    }

    public void updateInputs() {
        io.updateInputs(inputs);
        Logger.processInputs(key, inputs);
    }

    public T getIO() {
        return io;
    }

    public R getInputs() {
        return inputs;
    }

    public String getKey() {
        return key;
    }
}
