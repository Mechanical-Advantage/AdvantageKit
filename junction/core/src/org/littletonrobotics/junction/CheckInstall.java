package org.littletonrobotics.junction;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.util.WPILibVersion;
import java.io.File;
import java.io.FileInputStream;

public class CheckInstall {
  private CheckInstall() {
  }

  public static void main(String[] args) {
    run();
  }

  /**
   * Checks that the version of the installed WPILib shim matches the version of
   * WPILib in "build.gradle". A mismatch indicates that AdvantageKit is being
   * used with the wrong version of WPILib. If the check fails, a message will be
   * printed to stderr and the program will exit.
   *
   * <p>
   * This method cannot run on the roboRIO, only in simulation.
   */
  static void run() {
    String akitWPILibVersion = WPILibVersion.Version;
    String installedWPILibVersion = "";

    var buildFile = new File(Filesystem.getLaunchDirectory(), "build.gradle");
    try (FileInputStream inputStream = new FileInputStream(buildFile)) {
      String contents = new String(inputStream.readAllBytes());
      for (String line : contents.split("\n")) {
        if (line.contains("id \"edu.wpi.first.GradleRIO\" version ")) {
          installedWPILibVersion = line.split("\"")[3];
          break;
        }
      }
    } catch (Exception e) {
    }

    if (!akitWPILibVersion.equals(installedWPILibVersion)) {
      System.err.println(
          "The version of AdvantageKit installed in this project requires WPILib "
              + akitWPILibVersion
              + ", but "
              + (installedWPILibVersion.length() == 0
                  ? "an unknown version of WPILib"
                  : "WPILib " + installedWPILibVersion)
              + " is currently installed. Please update AdvantageKit and/or WPILib to compatible versions (the supported version of WPILib is listed in the release notes for each AdvantageKit version). DO NOT override this check; running with invalid versions will result in a broken project with issues that are difficult to diagnose.\n\n*** EXITING DUE TO INVALID ADVANTAGEKIT INSTALLATION, SEE ABOVE. ***");
      System.exit(1);
    }
  }
}