---
sidebar_position: 5
---

# Kotlin Support

Although advantagekit supports kotlin out of the box, there are a couple of features that do not work within kotlin files.

## @AutoLogOutput

Kotlin singletons(or objects) must be registered through a ```AutoLogOutput.addObject(this)``` call within the constructor. For instance:
```
object SomeSingleton {
    init {
        AutoLogOutput.addObject(this)
    }
    
    @AutoLogOutput
    fun getValue() = 2.0
}
```

## @AutoLog

If your team plans to use @AutoLog, it is recommended to paste the code below into a kotlin file named "AdvantageKitExtensions.kt"
(preferrably located within a "lib" or "util" folder of your robot code):

```kt
package your.lib.folder

import edu.wpi.first.units.Measure
import edu.wpi.first.units.MutableMeasure
import edu.wpi.first.util.struct.Struct
import edu.wpi.first.util.struct.StructSerializable
import org.littletonrobotics.junction.AutoLogOutputManager
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import kotlin.reflect.KProperty
import edu.wpi.first.units.Unit as WPIUnit

abstract class AutoLogInputs: LoggableInputs {
    fun log(value: Double, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun log(value: Int, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun log(value: String, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun log(value: Boolean, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun log(value: Long, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun <T: StructSerializable> log(value: T, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun <T> log(value: T, struct: Struct<T>, key: String? = null) =
        LoggedInput(value, key, {k,v -> put(k, struct, v)}, {k,v -> get(k, struct, v)})
    fun <U: WPIUnit> log(value: Measure<U>, key: String? = null) =
        LoggedInput(value, key, LogTable::put, LogTable::get)
    fun <U: WPIUnit, Base: Measure<U>, M: MutableMeasure<U, Base, M>> log(value: M, key: String? = null) =
        LoggedInput(value, key, LogTable::put, LogTable::get)

    fun log(value: DoubleArray, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun log(value: IntArray, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun log(value: Array<String>, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun log(value: BooleanArray, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun log(value: LongArray, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)
    fun <T: StructSerializable> log(value: Array<T>, key: String? = null) = LoggedInput(value, key, LogTable::put, LogTable::get)

    private val toLogRunners = mutableListOf<(LogTable) -> Unit>()
    private val fromLogRunners = mutableListOf<(LogTable) -> Unit>()

    inner class LoggedInput<T>(
        private var value: T,
        private val name: String? = null,
        private val toLog: LogTable.(String, T) -> Unit,
        private val fromLog: LogTable.(String, T) -> T,
    ) {
        operator fun getValue(thisRef: Any, property: KProperty<*>) = value
        operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) { this.value = value }

        operator fun provideDelegate(thisRef: Any, property: KProperty<*>): LoggedInput<T> {
            val namespace = this.name ?: property.name
            toLogRunners.add { logTable -> this.toLog(logTable, namespace, value) }
            fromLogRunners.add { logTable -> value = this.fromLog(logTable, namespace, value) }
            return this
        }
    }

    override fun fromLog(table: LogTable) {
        fromLogRunners.forEach { it(table) }
    }

    override fun toLog(table: LogTable) {
        toLogRunners.forEach { it(table) }
    }
}
```

This base class gives a replacement for the @AutoLog annotation for kotlin.

Here is an example of using auto-logged inputs in kotlin:
```kt
class ArmInputs: AutoLogInputs() {
    var angle by log(Rotation2d())
    var voltage by log(Volts.mutable(0.0), "ArmVoltage")
    var statorCurrent by log(Amps.mutable(2.0))
}

interface ArmIO {
    fun updateInputs(inputs: ArmInputs) {}
    fun setVoltage(volts: Voltage) {}
}

class Arm: SubsystemBase() {
    private val inputs = ArmInputs()
    private val io: ArmIO = ArmIOImpl()

    override fun periodic() {
        io.updateInputs(inputs)
        Logger.processInputs("Arm", inputs)
    }
}
```


