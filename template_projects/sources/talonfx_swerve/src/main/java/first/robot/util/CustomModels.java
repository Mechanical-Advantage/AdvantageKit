package first.robot.util;

import org.wpilib.math.linalg.MatBuilder;
import org.wpilib.math.linalg.Matrix;
import org.wpilib.math.linalg.VecBuilder;
import org.wpilib.math.numbers.N1;
import org.wpilib.math.numbers.N2;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.LinearSystem;
import org.wpilib.math.util.Nat;

public class CustomModels {
  /**
   * Create a state-space model of a DC motor system. The states of the system are [angular
   * position, angular velocity]ᵀ, inputs are [voltage], and outputs are [angular position, angular
   * velocity]ᵀ.
   *
   * @param motor The motor (or gearbox) attached to system.
   * @param JKgMetersSquared The moment of inertia J of the DC motor.
   * @param gearing The reduction between motor and drum, as a ratio of output to input.
   * @return A LinearSystem representing the given characterized constants.
   * @throws IllegalArgumentException if JKgMetersSquared &lt;= 0 or gearing &lt;= 0.
   */
  public static LinearSystem<N2, N1, N2> createDCMotorSystem(
      DCMotor motor, double JKgMetersSquared, double gearing) {
    if (JKgMetersSquared <= 0.0) {
      throw new IllegalArgumentException("J must be greater than zero.");
    }
    if (gearing <= 0.0) {
      throw new IllegalArgumentException("gearing must be greater than zero.");
    }

    return new LinearSystem<>(
        MatBuilder.fill(
            Nat.N2(),
            Nat.N2(),
            0,
            1,
            0,
            -gearing * gearing * motor.Kt / (motor.Kv * motor.R * JKgMetersSquared)),
        VecBuilder.fill(0, gearing * motor.Kt / (motor.R * JKgMetersSquared)),
        Matrix.eye(Nat.N2()),
        new Matrix<>(Nat.N2(), Nat.N1()));
  }
}
