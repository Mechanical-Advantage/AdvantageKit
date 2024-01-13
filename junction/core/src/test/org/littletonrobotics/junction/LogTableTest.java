package org.littletonrobotics.junction;

import edu.wpi.first.math.geometry.Rotation2d;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LogTableTest {
    private LogTable table;

    @BeforeEach
    void setup() {
        table = new LogTable(0);
    }

    @Test
    void supportsIntegers() {
        table.put("int", 5);
        Assertions.assertEquals(5, table.get("int").getInteger());
    }

    @Test
    void supportsIntArrays() {
        long[] expected = { 1, 2, 3 };

        table.put("int-array", expected);

        Assertions.assertArrayEquals(expected, table.get("int-array").getIntegerArray());
    }

    @Test
    void supportsProtobufSerialization() {
        Rotation2d expected = new Rotation2d(1, 2);

        // We're forcing protobuf based serialization so Struct based doesn't run
        table.put("rot", Rotation2d.proto, expected);

        Assertions.assertEquals(expected, table.get("rot", Rotation2d.proto, new Rotation2d()));
    }

    @Test
    void protobufIsntWrittenIfKeyAlreadyInUse() {
        Rotation2d rot = new Rotation2d(1, 2);

        table.put("rot", 5);
        table.put("rot", Rotation2d.proto, rot); // This should be skipped

        Assertions.assertNotEquals(rot, table.get("rot", Rotation2d.proto, new Rotation2d()));
        Assertions.assertEquals(5, table.get("rot").getInteger());
    }

    @Test
    void supportsStructSerialization() {
        Rotation2d expected = new Rotation2d(1, 2);

        // We're forcing Struct based serialization so Protobuf based doesn't run
        table.put("rot", Rotation2d.struct, expected);

        Assertions.assertEquals(expected, table.get("rot", Rotation2d.struct, new Rotation2d()));
    }

    @Test
    void supportsStructArraySerialization() {
        Rotation2d first = new Rotation2d(1, 2);
        Rotation2d second = new Rotation2d(3, 4);

        // We're forcing Struct serialization
        table.put("rot", Rotation2d.struct, first, second);

        Rotation2d[] actual = table.get("rot", Rotation2d.struct, new Rotation2d(0, 0), new Rotation2d(0,0));
        Assertions.assertArrayEquals(new Rotation2d[] { first, second }, actual);
    }
}
