package org.littletonrobotics.junction;

import com.squareup.javapoet.*;
import org.littletonrobotics.junction.inputs.LoggableInputs;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(AutoLog.class).forEach(classElement -> {
            MethodSpec.Builder toLogBuilder = MethodSpec.methodBuilder("toLog")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(LogTable.class, "table");
            MethodSpec.Builder fromLogBuilder = MethodSpec.methodBuilder("fromLog")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(LogTable.class, "table");

            List<FieldSpec> fields = new ArrayList<>();

            classElement.getEnclosedElements().stream().filter(f -> f.getKind().equals(ElementKind.FIELD)).forEach(fieldElement -> {
                String simpleName = fieldElement.getSimpleName().toString();
                String logName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
                toLogBuilder.addCode("table.put($S, $L);\n", logName, simpleName);
                String fieldType = fieldElement.asType().toString();
                String getterName = "get" + (fieldType.substring(0, 1).toUpperCase() + fieldType.substring(1)).replace("[]", "Array");
                fromLogBuilder.addCode("$L = table.$L($S, $L);\n", simpleName, getterName, logName, simpleName);

                //fields.add(FieldSpec.builder(TypeName.get(fieldElement.asType()), fieldElement.getSimpleName().toString(), fieldElement.getModifiers().toArray(Modifier[]::new)).build());
            });

            TypeSpec type = TypeSpec
                    .classBuilder(classElement.getSimpleName() + "AutoLogged")
                    .addSuperinterface(LoggableInputs.class)
                    .superclass(classElement.asType())
                    .addMethod(toLogBuilder.build())
                    .addMethod(fromLogBuilder.build())
                    .build();

            JavaFile file = JavaFile.builder(getPackageName(classElement), type).build();
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
