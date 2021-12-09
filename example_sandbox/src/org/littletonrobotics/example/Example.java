package org.littletonrobotics.example;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;

/**
 * Example
 */
public class Example {

    public static void main(String[] args) {
        HAL.initialize(500, 0);

        HAL.runMain();

        System.out.println(DriverStation.getInstance().getMatchNumber());
        
    }
}