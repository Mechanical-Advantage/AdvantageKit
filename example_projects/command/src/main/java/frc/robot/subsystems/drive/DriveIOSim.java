package frc.robot.subsystems.drive;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim.KitbotGearing;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim.KitbotMotor;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim.KitbotWheelSize;

public class DriveIOSim implements DriveIO {
  private DifferentialDrivetrainSim sim = DifferentialDrivetrainSim.createKitbotSim(KitbotMotor.kDualCIMPerSide,
      KitbotGearing.k10p71, KitbotWheelSize.kSixInch, null);

  @Override
  public void updateInputs(DriveIOInputs inputs) {
    sim.update(0.02);
    inputs.leftPositionRad = sim.getLeftPositionMeters() / Drive.WHEEL_RADIUS_METERS;
    inputs.leftVelocityRadPerSec = sim.getLeftVelocityMetersPerSecond() / Drive.WHEEL_RADIUS_METERS;
    inputs.rightPositionRad = sim.getRightPositionMeters() / Drive.WHEEL_RADIUS_METERS;
    inputs.rightVelocityRadPerSec = sim.getRightVelocityMetersPerSecond() / Drive.WHEEL_RADIUS_METERS;
    inputs.gyroYawRad = sim.getHeading().getRadians() * -1;
  }

  @Override
  public void setVoltage(double leftVolts, double rightVolts) {
    sim.setInputs(MathUtil.clamp(leftVolts, -12.0, 12.0), MathUtil.clamp(rightVolts, -12.0, 12.0));
  }
}
