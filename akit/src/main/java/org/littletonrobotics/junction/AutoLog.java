// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generate a corresponding auto-logged inputs class implementing {@link
 * org.littletonrobotics.junction.inputs.LoggableInputs LoggableInputs}. Check the <a href=
 * "https://docs.advantagekit.org/data-flow/recording-inputs/annotation-logging">documentation</a>
 * for details.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface AutoLog {}
