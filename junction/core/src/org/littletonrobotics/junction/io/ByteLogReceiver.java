package org.littletonrobotics.junction.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.MatchType;

/** Records log values to a custom binary format. */
public class ByteLogReceiver implements LogRawDataReceiver {

  private String folder;
  private String filename;

  private boolean autoRename = false;
  private boolean updatedTime = false;
  private boolean updatedMatch = false;

  private FileOutputStream fileStream;
  private ByteEncoder encoder;

  /**
   * Create a new ByteLogReceiver for writing to a ".rlog" file.
   * 
   * @param path Path to log file or folder. If only a folder is provided, the
   *             filename will be generated based on the current time and match
   *             number (if applicable).
   */
  public ByteLogReceiver(String path) {
    if (path.endsWith(".rlog")) {
      File pathFile = new File(path);
      folder = pathFile.getParent() + "/";
      filename = pathFile.getName();
      autoRename = false;
    } else if (path.endsWith("/")) {
      folder = path;
      filename = "temp.rlog";
      autoRename = true;
    } else {
      folder = path + "/";
      filename = "temp.rlog";
      autoRename = true;
    }
  }

  public void start(ByteEncoder encoder) {
    this.encoder = encoder;
    try {
      new File(folder + filename).delete();
      fileStream = new FileOutputStream(folder + filename);
    } catch (FileNotFoundException e) {
      DriverStation.reportError("Failed to open log file. Data will NOT be recorded.", true);
    }
  }

  public void end() {
    if (fileStream != null) {
      try {
        fileStream.close();
        fileStream = null;
      } catch (IOException e) {
        DriverStation.reportError("Failed to close log file.", true);
      }
    }
  }

  public void processEntry() {
    if (fileStream != null) {
      // Auto rename
      if (autoRename) {
        if (System.currentTimeMillis() > 1638334800000L) { // 12/1/2021, the RIO 2 defaults to 7/1/2021
          if (!updatedTime) {
            rename(new SimpleDateFormat("'Log'_yy-MM-dd_HH-mm-ss'.rlog'").format(new Date()));
            updatedTime = true;
          }

          if (DriverStation.getMatchType() != MatchType.None && !updatedMatch) {
            String matchText = "";
            switch (DriverStation.getMatchType()) {
              case Practice:
                matchText = "p";
                break;
              case Qualification:
                matchText = "q";
                break;
              case Elimination:
                matchText = "e";
                break;
              default:
                break;
            }
            matchText += Integer.toString(DriverStation.getMatchNumber());
            rename(filename.substring(0, filename.length() - 5) + "_" + matchText + ".rlog");
            updatedMatch = true;
          }
        }
      }

      // Write data
      try {
        fileStream.write(encoder.getOutput().array());
      } catch (IOException e) {
        DriverStation.reportError("Failed to write data to log file.", true);
      }
    }

  }

  private void rename(String newFilename) {
    File oldFile = new File(folder + filename);
    File newFile = new File(folder + newFilename);
    oldFile.renameTo(newFile);
    filename = newFilename;

    try {
      fileStream.close();
      fileStream = new FileOutputStream(folder + filename, true);
    } catch (IOException e) {
      DriverStation.reportError("Failed to rename log file.", true);
    }
  }

  /**
   * Adds a suffix to the given path (e.g. "test.rlog" -> "test_simulated.rlog").
   */
  public static String addPathSuffix(String path, String suffix) {
    String[] tokens = path.split("\\.");
    return tokens[0] + suffix + "." + tokens[1];
  }
}