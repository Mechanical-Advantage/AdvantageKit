// Copyright (c) 2021-2025 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import com.squareup.javapoet.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class AutoLogAnnotationProcessor extends AbstractProcessor {
  private static String getPackageName(Element e) {
    while (e != null) {
      if (e.getKind().equals(ElementKind.PACKAGE)) {
        return ((PackageElement) e).getQualifiedName().toString();
      }
      e = e.getEnclosingElement();
    }

    return null;
  }

  private static final TypeName LOG_TABLE_TYPE =
      ClassName.get("org.littletonrobotics.junction", "LogTable");
  private static final TypeName LOGGABLE_INPUTS_TYPE =
      ClassName.get("org.littletonrobotics.junction.inputs", "LoggableInputs");
  private static final Map<String, String> UNLOGGABLE_TYPES_SUGGESTIONS = new HashMap<>();

  static {
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Byte[]", "byte[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Boolean", "boolean");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Integer", "int");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Long", "long");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Float", "float");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Double", "double");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Boolean[]", "boolean[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Integer[]", "int[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Long[]", "long[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Float[]", "float[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Double[]", "double[]");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Optional<? extends TypeElement> annotationOptional =
        annotations.stream()
            .filter((te) -> te.getSimpleName().toString().equals("AutoLog"))
            .findFirst();
    if (!annotationOptional.isPresent()) {
      return false;
    }

    TypeElement annotation = annotationOptional.get();
    roundEnv
        .getElementsAnnotatedWith(annotation)
        .forEach(
            classElement -> {
              String autologgedClassName = classElement.getSimpleName() + "AutoLogged";
              String autologgedPackage = getPackageName(classElement);

              MethodSpec.Builder toLogBuilder =
                  MethodSpec.methodBuilder("toLog")
                      .addAnnotation(Override.class)
                      .addModifiers(Modifier.PUBLIC)
                      .addParameter(LOG_TABLE_TYPE, "table");
              MethodSpec.Builder fromLogBuilder =
                  MethodSpec.methodBuilder("fromLog")
                      .addAnnotation(Override.class)
                      .addModifiers(Modifier.PUBLIC)
                      .addParameter(LOG_TABLE_TYPE, "table");
              MethodSpec.Builder cloneBuilder =
                  MethodSpec.methodBuilder("clone")
                      .addModifiers(Modifier.PUBLIC)
                      .addCode("$L copy = new $L();\n", autologgedClassName, autologgedClassName)
                      .returns(ClassName.get(autologgedPackage, autologgedClassName));

              Types util = processingEnv.getTypeUtils();
              TypeElement typeElement = (TypeElement) classElement;
              boolean isSuperclass = false;
              while (typeElement != null) {
                final TypeElement finalTypeElement = typeElement;
                final boolean finalIsSuperclass = isSuperclass;
                typeElement.getEnclosedElements().stream()
                    .filter(f -> f.getKind().equals(ElementKind.FIELD))
                    .forEach(
                        fieldElement -> {
                          if (finalIsSuperclass
                              && fieldElement.getModifiers().contains(Modifier.PRIVATE)) {
                            return;
                          }

                          String simpleName = fieldElement.getSimpleName().toString();
                          String logName =
                              simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);

                          String fieldType = fieldElement.asType().toString();
                          String typeSuggestion = UNLOGGABLE_TYPES_SUGGESTIONS.get(fieldType);

                          // Check for unloggable types
                          if (typeSuggestion != null
                              || (fieldType.startsWith("java")
                                  && !fieldType.startsWith("java.lang.String"))) {
                            String extraText = "";
                            if (typeSuggestion != null) {
                              extraText = "Did you mean to use \"" + typeSuggestion + "\" instead?";
                            } else {
                              extraText = "\"" + fieldType + "\" is not supported";
                            }
                            throw new RuntimeException(
                                "[AutoLog] Unkonwn type for \""
                                    + simpleName
                                    + "\" from \""
                                    + finalTypeElement.getSimpleName()
                                    + "\" ("
                                    + extraText
                                    + ")");
                          }

                          // Log data (might be serialized)
                          toLogBuilder.addCode("table.put($S, $L);\n", logName, simpleName);
                          fromLogBuilder.addCode(
                              "$L = table.get($S, $L);\n", simpleName, logName, simpleName);
                          if (fieldElement.asType().getKind().equals(TypeKind.ARRAY)) {
                            // Need to deep copy arrays
                            cloneBuilder.addCode(
                                "copy.$L = this.$L.clone();\n", simpleName, simpleName);
                          } else if (fieldElement
                              .asType()
                              .toString()
                              .startsWith("edu.wpi.first.units.MutableMeasure")) {
                            // Need to clone mutable measure
                            cloneBuilder.addCode(
                                "copy.$L = this.$L.mutableCopy();\n", simpleName, simpleName);
                          } else {
                            cloneBuilder.addCode("copy.$L = this.$L;\n", simpleName, simpleName);
                          }
                        });
                TypeMirror mirror = (typeElement).getSuperclass();
                if (mirror.getKind() == TypeKind.DECLARED) {
                  typeElement = (TypeElement) util.asElement(mirror);
                  isSuperclass = true;
                } else {
                  typeElement = null;
                }
              }

              cloneBuilder.addCode("return copy;\n");

              TypeSpec type =
                  TypeSpec.classBuilder(autologgedClassName)
                      .addModifiers(Modifier.PUBLIC)
                      .addSuperinterface(LOGGABLE_INPUTS_TYPE)
                      .addSuperinterface(ClassName.get("java.lang", "Cloneable"))
                      .superclass(classElement.asType())
                      .addMethod(toLogBuilder.build())
                      .addMethod(fromLogBuilder.build())
                      .addMethod(cloneBuilder.build())
                      .build();

              JavaFile file = JavaFile.builder(autologgedPackage, type).build();
              try {
                file.writeTo(processingEnv.getFiler());
              } catch (IOException e) {
                processingEnv
                    .getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, "Failed to write class", classElement);
                e.printStackTrace();
              }
            });
    return true;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of("org.littletonrobotics.junction.AutoLog");
  }
}
