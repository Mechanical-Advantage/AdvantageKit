package frc.robot.subsystems.flywheel;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.SparkMaxPIDController.ArbFFUnits;

import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.RobotController;

public class FlywheelIOSparkMax implements FlywheelIO {
  private static final double GEAR_RATIO = 1.5;

  private final CANSparkMax leader;
  private final CANSparkMax follower;
  private final RelativeEncoder encoder;
  private final SparkMaxPIDController pid;

  public FlywheelIOSparkMax() {
    leader = new CANSparkMax(5, MotorType.kBrushless);
    follower = new CANSparkMax(6, MotorType.kBrushless);

    encoder = leader.getEncoder();
    pid = leader.getPIDController();

    leader.restoreFactoryDefaults();
    follower.restoreFactoryDefaults();

    leader.setInverted(false);
    follower.follow(leader, false);

    leader.enableVoltageCompensation(12.0);
    leader.setSmartCurrentLimit(30);

    leader.burnFlash();
    follower.burnFlash();
  }

  @Override
  public void updateInputs(FlywheelIOInputs inputs) {
    inputs.positionRad = Units.rotationsToRadians(encoder.getPosition() / GEAR_RATIO);
    inputs.velocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(
        encoder.getVelocity() / GEAR_RATIO);
    inputs.appliedVolts = leader.getAppliedOutput() * RobotController.getBatteryVoltage();
    inputs.currentAmps = new double[] { leader.getOutputCurrent(), follower.getOutputCurrent() };
  }

  @Override
  public void setVelocity(double velocityRadPerSec, double ffVolts) {
    pid.setReference(
        Units.radiansPerSecondToRotationsPerMinute(velocityRadPerSec)
            * GEAR_RATIO,
        ControlType.kVelocity, 0, ffVolts, ArbFFUnits.kVoltage);
  }

  @Override
  public void stop() {
    leader.stopMotor();
  }

  @Override
  public void configurePID(double kP, double kI, double kD) {
    pid.setP(kP, 0);
    pid.setI(kI, 0);
    pid.setD(kD, 0);
    pid.setFF(0, 0);
  }
}
