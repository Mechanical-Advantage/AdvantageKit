import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class Sigcheck {
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

        patchScan.getAllClasses().stream().map((x) -> x.getName()).collect(Collectors.toList()).forEach(System.out::println);
        System.out.println("-----------------------");
        origScan.getAllClasses().stream().map((x) -> x.getName()).collect(Collectors.toList()).forEach(System.out::println);
    }
}
