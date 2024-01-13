package org.littletonrobotics.junction;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.proto.Rotation2dProto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.LogTable.LogValue;

public class LogTableTest {
    @Test
    void supportsIntegers() {
        LogTable table = new LogTable(0);
        table.put("int", 5);
        LogValue val = table.get("int");
        Assertions.assertEquals(5, val.getInteger());
    }

    @Test
    void supportsIntArrays() {
        long[] expected = { 1, 2, 3 };

        LogTable table = new LogTable(0);
        table.put("int-array", expected);

        LogValue val = table.get("int-array");
        Assertions.assertArrayEquals(expected, val.getIntegerArray());
    }

    @Test
    void supportsProtobufSerialization() {
        Rotation2d expected = new Rotation2d(1, 2);

        LogTable table = new LogTable(0);
        // We're forcing protobuf based serialization so Struct based doesn't run
        table.put("rot", Rotation2d.proto, expected);

        Rotation2d actual = table.get("rot", Rotation2d.proto, new Rotation2d());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void supportsStructSerialization() {
        Rotation2d expected = new Rotation2d(1, 2);

        LogTable table = new LogTable(0);
        // We're forcing Struct based serialization so Protobuf based doesn't run
        table.put("rot", Rotation2d.struct, expected);

        Rotation2d actual = table.get("rot", Rotation2d.struct, new Rotation2d());
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void supportsStructArraySerialization() {
        Rotation2d first = new Rotation2d(1, 2);
        Rotation2d second = new Rotation2d(3, 4);

        LogTable table = new LogTable(0);
        // We're forcing Struct serialization
        table.put("rot", Rotation2d.struct, first, second);

        Rotation2d[] actual = table.get("rot", Rotation2d.struct, new Rotation2d(0, 0), new Rotation2d(0,0));
        Assertions.assertArrayEquals(new Rotation2d[] { first, second }, actual);
    }
}
