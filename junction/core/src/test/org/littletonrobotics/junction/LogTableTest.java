package org.littletonrobotics.junction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.littletonrobotics.junction.LogTable.LogValue;

public class LogTableTest {
    @Test
    void logTableCanRoundtripIntegers() {
        LogTable table = new LogTable(0);
        table.put("int", 5);
        LogValue val = table.get("int");
        Assertions.assertEquals(5, val.getInteger());
    }

    @Test
    void logTableCanRoundtripIntArrays() {
        long[] expected = { 1, 2, 3 };

        LogTable table = new LogTable(0);
        table.put("int-array", expected);

        LogValue val = table.get("int-array");
        Assertions.assertArrayEquals(expected, val.getIntegerArray());
    }
}
