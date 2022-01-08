import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;

public class Sigcheck {
    private static boolean compareClasses(ClassInfo original, ClassInfo patch) {
        List<String> failures = new ArrayList<>();
        // Ensure patch class has same fields as original
        for (FieldInfo originalField : original.getFieldInfo()) {
            if (!originalField.isPublic())
                continue; // Skip non-public fields (not part of the API)
            // Check that the patch class has the field

            FieldInfo patchField = patch.getFieldInfo(originalField.getName());
            if (patchField != null) {
                List<String> fieldFailures = new ArrayList<>();
                // Field is present, compare
                if (!originalField.getModifiersStr().equals(patchField.getModifiersStr())) {
                    fieldFailures.add("\t  -> Expected modifiers '" + originalField.getModifiersStr() + "' but got '"
                            + patchField.getModifiersStr() + "'");
                }
                if (!originalField.getTypeSignatureOrTypeDescriptorStr()
                        .equals(patchField.getTypeSignatureOrTypeDescriptorStr())) {
                    // Intentionally not using getTypeSignatureOrTypeDescriptorStr as this method
                    // doesn't return nice names for primitives
                    fieldFailures.add("\t  -> Expected type '" + originalField.getTypeSignatureOrTypeDescriptor()
                            + "' but got '" + patchField.getTypeSignatureOrTypeDescriptor() + "'");
                }

                if (!fieldFailures.isEmpty()) {
                    failures.add("\t-> For field '" + originalField.getName() + "'");
                    failures.addAll(fieldFailures);
                }

            } else {
                failures.add("\t-> Missing field '" + originalField + "'");
            }

        }

        // Ensure patch class has same methods as original
        for (MethodInfo originalMethod : original.getMethodAndConstructorInfo()) {
            if (!originalMethod.isPublic())
                continue; // Skip non-public methods (not part of the API)
            // Check that the patch class has the method

            MethodInfo patchMethod = patch.getMethodInfo(originalMethod.getName()).stream()
                    .filter(originalMethod::equals).findFirst().orElse(null);
            if (patchMethod == null) {
                failures.add("\t-> Missing method '" + originalMethod + "'");
                // The method is either outright missing or incorrectly defined
                // Reverse lookup in the original class to see if there are any methods defined
                // in the patch that aren't defined in the original

                List<String> mismatchedMethods = new ArrayList<>();
                for (MethodInfo m : patch.getMethodInfo(originalMethod.getName())) {
                    if (!original.getMethodAndConstructorInfo().contains(m)) {
                        mismatchedMethods.add("\t    -> '" + m + "'");
                    }
                }

                if (!mismatchedMethods.isEmpty()) {
                    failures.add("\t  -> Found " + mismatchedMethods.size() + (mismatchedMethods.size() == 1 ? " method" : " methods") + " with same name in patch that " + (mismatchedMethods.size() == 1 ? "does" : "do") + " not match any original method signatures");
                    failures.addAll(mismatchedMethods);
                }
            }
        }

        if (!failures.isEmpty()) {
            System.out.println("[FAIL] For class '" + original.getName() + "'");
            failures.forEach(System.out::println);
            return false;
        } else {
            System.out.println("[PASS] Class '" + original.getName() + "'");
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        File patchJar = new File(args[0]);
        File origJar = new File(args[1]);

        ScanResult patchScan = new ClassGraph()
                .overrideClasspath(patchJar.getPath())
                .enableAllInfo()
                .scan();

        ScanResult origScan = new ClassGraph()
                .overrideClasspath(origJar.getPath())
                .enableAllInfo()
                .scan();

        ClassInfoList origClasses = origScan.getAllClasses();
        ClassInfoList patchClasses = patchScan.getAllClasses();

        int failures = 0;
        int passes = 0;

        for (ClassInfo origClass : origClasses) {
            ClassInfo patchClass = patchClasses.get(origClass.getName());
            if (patchClass != null) {
                if (compareClasses(origClass, patchClass)) {
                    passes++;
                } else {
                    failures++;
                }
            }
        }

        System.out.println("Analyzed " + origClasses.size() + " original classes, " + patchClasses.size() + " patch classes");
        System.out.println(passes + " passed, " + failures + " failed");

        if (failures != 0) System.exit(1);
    }
}
