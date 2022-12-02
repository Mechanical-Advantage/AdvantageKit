package org.littletonrobotics.junction;

import com.squareup.javapoet.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

  private static final TypeName LOG_TABLE_TYPE = ClassName.get("org.littletonrobotics.junction", "LogTable");
  private static final TypeName LOGGABLE_INPUTS_TYPE = ClassName.get("org.littletonrobotics.junction.inputs",
      "LoggableInputs");
  private static final Map<String, String> LOGGABLE_TYPES_LOOKUP = new HashMap<>();
  private static final Map<String, String> UNLOGGABLE_TYPES_SUGGESTIONS = new HashMap<>();

  static {
    LOGGABLE_TYPES_LOOKUP.put("byte[]", "Raw");
    LOGGABLE_TYPES_LOOKUP.put("boolean", "Boolean");
    LOGGABLE_TYPES_LOOKUP.put("long", "Integer");
    LOGGABLE_TYPES_LOOKUP.put("float", "Float");
    LOGGABLE_TYPES_LOOKUP.put("double", "Double");
    LOGGABLE_TYPES_LOOKUP.put("java.lang.String", "String");
    LOGGABLE_TYPES_LOOKUP.put("boolean[]", "BooleanArray");
    LOGGABLE_TYPES_LOOKUP.put("long[]", "IntegerArray");
    LOGGABLE_TYPES_LOOKUP.put("float[]", "FloatArray");
    LOGGABLE_TYPES_LOOKUP.put("double[]", "DoubleArray");
    LOGGABLE_TYPES_LOOKUP.put("java.lang.String[]", "StringArray");

    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Byte[]", "byte[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Boolean", "boolean");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Long", "long");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("int", "long");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Integer", "long");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Float", "float");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Double", "double");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Boolean[]", "boolean[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Long[]", "long[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("int[]", "long[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Integer[]", "long[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Float[]", "float[]");
    UNLOGGABLE_TYPES_SUGGESTIONS.put("java.lang.Double[]", "double[]");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Optional<? extends TypeElement> annotationOptional = annotations.stream()
        .filter((te) -> te.getSimpleName().toString().equals("AutoLog")).findFirst();
    if (!annotationOptional.isPresent()) {
      return false;
    }

    TypeElement annotation = annotationOptional.get();
    roundEnv.getElementsAnnotatedWith(annotation).forEach(classElement -> {
      String autologgedClassName = classElement.getSimpleName() + "AutoLogged";
      String autologgedPackage = getPackageName(classElement);

      MethodSpec.Builder toLogBuilder = MethodSpec.methodBuilder("toLog")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC)
          .addParameter(LOG_TABLE_TYPE, "table");
      MethodSpec.Builder fromLogBuilder = MethodSpec.methodBuilder("fromLog")
          .addAnnotation(Override.class)
          .addModifiers(Modifier.PUBLIC)
          .addParameter(LOG_TABLE_TYPE, "table");
      MethodSpec.Builder cloneBuilder = MethodSpec.methodBuilder("clone")
          .addModifiers(Modifier.PUBLIC)
          .addCode("$L copy = new $L();\n", autologgedClassName, autologgedClassName)
          .returns(ClassName.get(autologgedPackage, autologgedClassName));

      classElement.getEnclosedElements().stream().filter(f -> f.getKind().equals(ElementKind.FIELD))
          .forEach(fieldElement -> {
            String simpleName = fieldElement.getSimpleName().toString();
            String logName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);

            String fieldType = fieldElement.asType().toString();
            String logType = LOGGABLE_TYPES_LOOKUP.get(fieldType);
            if (logType == null) {
              String typeSuggestion = UNLOGGABLE_TYPES_SUGGESTIONS.get(fieldType);
              String extraText = "";
              if (typeSuggestion != null) {
                extraText = "Did you mean to use \"" + typeSuggestion + "\" instead?";
              } else {
                extraText = "\"" + fieldType + "\" is not supported";
              }
              System.err.println(
                  "[AutoLog] Unkonwn type for \"" + simpleName + "\" from \"" + classElement.getSimpleName()
                      + " (" + extraText + ")");
            } else {
              String getterName = "get" + logType;
              toLogBuilder.addCode("table.put($S, $L);\n", logName, simpleName);
              fromLogBuilder.addCode("$L = table.$L($S, $L);\n", simpleName, getterName, logName, simpleName);
              // Need to deep copy arrays
              if (fieldElement.asType().getKind().equals(TypeKind.ARRAY)) {
                cloneBuilder.addCode("copy.$L = this.$L.clone();\n", simpleName, simpleName);
              } else {
                cloneBuilder.addCode("copy.$L = this.$L;\n", simpleName, simpleName);
              }
            }
          });

      cloneBuilder.addCode("return copy;\n");

      TypeSpec type = TypeSpec
          .classBuilder(autologgedClassName)
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
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to write class", classElement);
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
