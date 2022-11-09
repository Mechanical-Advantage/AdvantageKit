package frc.robot.subsystems.flywheel;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.DemandType;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;

import edu.wpi.first.math.util.Units;

public class FlywheelIOFalcon500 implements FlywheelIO {
  private static final double GEAR_RATIO = 1.5;
  private static final double TICKS_PER_REV = 2048;

  private final TalonFX leader;
  private final TalonFX follower;

  public FlywheelIOFalcon500() {
    leader = new TalonFX(5);
    follower = new TalonFX(6);

    TalonFXConfiguration config = new TalonFXConfiguration();
    config.voltageCompSaturation = 12.0;
    config.statorCurrLimit.enable = true;
    config.statorCurrLimit.currentLimit = 40;
    config.primaryPID.selectedFeedbackSensor = FeedbackDevice.IntegratedSensor;
    leader.configAllSettings(config);

    follower.follow(leader);
    leader.setInverted(false);
    follower.setInverted(InvertType.FollowMaster);
  }

  @Override
  public void updateInputs(FlywheelIOInputs inputs) {
    inputs.positionRad = Units.rotationsToRadians(
        leader.getSelectedSensorPosition() / TICKS_PER_REV / GEAR_RATIO);
    inputs.velocityRadPerSec = Units.rotationsPerMinuteToRadiansPerSecond(
        leader.getSelectedSensorVelocity() * 10 / TICKS_PER_REV / GEAR_RATIO);
    inputs.appliedVolts = leader.getMotorOutputVoltage();
    inputs.currentAmps = new double[] { leader.getSupplyCurrent(),
        follower.getSupplyCurrent() };
  }

  @Override
  public void setVelocity(double velocityRadPerSec, double ffVolts) {
    double velocityFalconUnits = Units.radiansToRotations(velocityRadPerSec)
        * GEAR_RATIO * TICKS_PER_REV / 10.0;
    leader.set(ControlMode.Velocity, velocityFalconUnits,
        DemandType.ArbitraryFeedForward, ffVolts / 12.0);
  }

  @Override
  public void stop() {
    leader.set(ControlMode.PercentOutput, 0.0);
  }

  @Override
  public void configurePID(double kP, double kI, double kD) {
    leader.config_kP(0, kP);
    leader.config_kI(0, kI);
    leader.config_kD(0, kD);
    leader.config_kF(0, 0.0);
  }
}
