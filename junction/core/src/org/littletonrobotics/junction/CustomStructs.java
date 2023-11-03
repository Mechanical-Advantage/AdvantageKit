// Copyright 2021-2023 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.

package org.littletonrobotics.junction;

import java.nio.ByteBuffer;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.util.struct.Struct;

public final class CustomStructs {
    public static final class ASwerveModuleState implements Struct<SwerveModuleState> {
        @Override
        public Class<SwerveModuleState> getTypeClass() {
            return SwerveModuleState.class;
        }

        @Override
        public String getTypeString() {
            return "struct:SwerveModuleState";
        }

        @Override
        public int getSize() {
            return kSizeDouble + Rotation2d.struct.getSize();
        }

        @Override
        public String getSchema() {
            return "double speed;Rotation2d angle";
        }

        @Override
        public Struct<?>[] getNested() {
            return new Struct<?>[] { Rotation2d.struct };
        }

        @Override
        public SwerveModuleState unpack(ByteBuffer bb) {
            double speed = bb.getDouble();
            Rotation2d angle = Rotation2d.struct.unpack(bb);
            return new SwerveModuleState(speed, angle);
        }

        @Override
        public void pack(ByteBuffer bb, SwerveModuleState value) {
            bb.putDouble(value.speedMetersPerSecond);
            Rotation2d.struct.pack(bb, value.angle);
        }
    }

    public static final ASwerveModuleState swerveModuleState = new ASwerveModuleState();
}