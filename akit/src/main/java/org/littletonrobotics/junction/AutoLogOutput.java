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
 * Automatically record the field or method as an output. Check the <a href=
 * "https://docs.advantagekit.org/data-flow/recording-outputs/annotation-logging">documentation</a>
 * for details.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface AutoLogOutput {
  /**
   * The key to use when logging the field or method. Use {...} to reference constant fields for
   * disambiguation.
   *
   * @return The value of the key parameter.
   */
  public String key() default "";

  /**
   * Whether or not to force the Logger to use a serialized data method.
   *
   * @return Whether or not to force the Logger to use a serialized data method.
   */
  public boolean forceSerializable() default false;

  /**
   * The unit to save as metadata, used when visualizing the field in AdvantageScope.
   *
   * @return The value of the unit parameter.
   */
  public String unit() default "";
}
