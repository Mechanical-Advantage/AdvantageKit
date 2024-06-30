package org.littletonrobotics.junction.inputs;

public interface LoggableIO<T extends LoggableInputs> {
    void updateInputs(T inputs);
}
