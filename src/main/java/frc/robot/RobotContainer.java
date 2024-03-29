// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Constants.DrivetrainConstants;
import frc.robot.subsystems.ExampleSubsystem;
import frc.robot.subsystems.LegSubsystem;
import frc.robot.subsystems.TankDriveSubsystem;

import com.kauailabs.navx.frc.AHRS;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final ExampleSubsystem m_exampleSubsystem = new ExampleSubsystem();

  private final RobotBase robot;

  private CANSparkMax backRightSpeedMotor = new CANSparkMax(DrivetrainConstants.BOTTOM_RIGHT_SPEED_ID,
      MotorType.kBrushed);

  private CANSparkMax frontRightSpeedMotor = new CANSparkMax(DrivetrainConstants.TOP_RIGHT_SPEED_ID,
      MotorType.kBrushed);

  private CANSparkMax backLeftSpeedMotor = new CANSparkMax(DrivetrainConstants.BOTTOM_LEFT_SPEED_ID,
      MotorType.kBrushed);

  private CANSparkMax frontLeftSpeedMotor = new CANSparkMax(DrivetrainConstants.TOP_LEFT_SPEED_ID, MotorType.kBrushed);

  AHRS gyro = new AHRS(SPI.Port.kMXP);

  private TankDriveSubsystem tank = new TankDriveSubsystem(backLeftSpeedMotor, backRightSpeedMotor, frontLeftSpeedMotor,
      frontRightSpeedMotor);
  private DoubleSolenoid solenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 0, 1);
  private LegSubsystem leg = new LegSubsystem(solenoid);

  SlewRateLimiter limitX = new SlewRateLimiter(6);
  SlewRateLimiter limitY = new SlewRateLimiter(6);

  private boolean limiter = true;
  private boolean coast = false;

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer(RobotBase robot) {
    this.robot = robot;
    // Configure the trigger bindings
    configureBindings();
    configureObjects();
  }

  public void configureObjects() {
    resetMotors();
    if (coast) {
      coastDrive();
    } else {
      brakeDrive();
    }

  }

  private void configureBindings() {
    Joystick leftJoystick = new Joystick(0);
    Joystick rightJoystick = new Joystick(1);
    Joystick altJoystick = new Joystick(2);

    JoystickButton kickButton = new JoystickButton(rightJoystick, 1);
    JoystickButton limiterToggleButton = new JoystickButton(rightJoystick, 5);
    JoystickButton limiterToggleButton1 = new JoystickButton(rightJoystick, 6);
    JoystickButton coastToggleButton = new JoystickButton(rightJoystick, 11);

    tank.setDefaultCommand(new RunCommand(() -> {
      // System.out.println("1");
      if (robot.isTeleopEnabled()) {
        // System.out.println("2");
        if (limiter) {
          tank.spin(
              limitX.calculate(applyDeadband(leftJoystick.getY(), DrivetrainConstants.DRIFT_DEADBAND) * 0.3),
              limitY.calculate(applyDeadband(-rightJoystick.getY(), DrivetrainConstants.DRIFT_DEADBAND) * 0.3));
        } else {
          tank.spin(
              limitX.calculate(applyDeadband(leftJoystick.getY(), DrivetrainConstants.DRIFT_DEADBAND) * 0.8),
              limitY.calculate(applyDeadband(-rightJoystick.getY(), DrivetrainConstants.DRIFT_DEADBAND) * 0.8));
        }
      } else {
        tank.spin(0, 0);
      }
    }, tank));

    kickButton.onTrue(new InstantCommand(() -> {
      leg.toggle();
    }, leg));
    
    coastToggleButton.onTrue(new InstantCommand(() -> {
      coast = !coast;
      System.out.println("Coast Enabled: " + coast);
      configureObjects();
    }));

    limiterToggleButton.and(limiterToggleButton1).onTrue(new InstantCommand(() -> {
      limiter = !limiter;
      System.out.println("Limiter Enabled: " + limiter);
    }));
  }

  public double applyDeadband(double input, double deadband) {
    if (Math.abs(input) < deadband)
      return 0;
    else
      return input;
  }

  public void resetMotors() {
    frontLeftSpeedMotor.restoreFactoryDefaults();
    frontRightSpeedMotor.restoreFactoryDefaults();
    backLeftSpeedMotor.restoreFactoryDefaults();
    backRightSpeedMotor.restoreFactoryDefaults();
  }

  public void coastDrive() {
    frontLeftSpeedMotor.setIdleMode(IdleMode.kCoast);
    frontRightSpeedMotor.setIdleMode(IdleMode.kCoast);
    backLeftSpeedMotor.setIdleMode(IdleMode.kCoast);
    backRightSpeedMotor.setIdleMode(IdleMode.kCoast);
  }

  public void brakeDrive() {
    frontLeftSpeedMotor.setIdleMode(IdleMode.kBrake);
    frontRightSpeedMotor.setIdleMode(IdleMode.kBrake);
    backLeftSpeedMotor.setIdleMode(IdleMode.kBrake);
    backRightSpeedMotor.setIdleMode(IdleMode.kBrake);
  }
}
