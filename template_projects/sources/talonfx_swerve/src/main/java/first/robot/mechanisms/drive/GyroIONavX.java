// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package first.robot.mechanisms.drive;

import static org.wpilib.units.Units.*;

import com.studica.frc.Navx;
import com.studica.frc.Navx.AngularVelocity3d;
import java.util.Queue;
import org.wpilib.hardware.bus.CANPort;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.units.measure.Time;

/** IO implementation for NavX. */
public class GyroIONavX implements GyroIO {
  private final int NAVX_CAN_ID = 1;
  private final CANPort NAVX_CAN_PORT = CANPort.CAN_D0;
  private final Navx navX = new Navx(NAVX_CAN_ID, NAVX_CAN_PORT);
  private final Queue<Double> yawPositionQueue;
  private final Queue<Double> yawTimestampQueue;

  public GyroIONavX() {
    yawTimestampQueue = PhoenixOdometryThread.getInstance().makeTimestampQueue();
    yawPositionQueue =
        PhoenixOdometryThread.getInstance()
            .registerSignal(() -> navX.getYaw().orElse(Radians.of(Double.NaN)).in(Degrees));
  }

  @Override
  public void updateInputs(GyroIOInputs inputs) {
    inputs.connected = navX.isConnected();
    inputs.yawPosition = new Rotation2d(navX.getYaw().orElse(Radians.of(Double.NaN)).unaryMinus());
    inputs.yawVelocity =
        navX.getAngularVelocity()
            .orElse(new AngularVelocity3d(null, null, RadiansPerSecond.of(Double.NaN)))
            .z()
            .unaryMinus();

    inputs.odometryYawTimestamps =
        yawTimestampQueue.stream().map((Double value) -> Seconds.of(value)).toArray(Time[]::new);
    inputs.odometryYawPositions =
        yawPositionQueue.stream()
            .map((Double value) -> Rotation2d.fromDegrees(-value))
            .toArray(Rotation2d[]::new);
    yawTimestampQueue.clear();
    yawPositionQueue.clear();
  }
}
