// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package org.littletonrobotics.junction;

import static org.wpilib.units.Units.Seconds;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.wpilib.driverstation.DriverStationErrors;
import org.wpilib.driverstation.RobotState;
import org.wpilib.driverstation.internal.DriverStationBackend;
import org.wpilib.framework.RobotBase;
import org.wpilib.hardware.hal.ControlWord;
import org.wpilib.hardware.hal.DriverStationJNI;
import org.wpilib.hardware.hal.HAL;
import org.wpilib.hardware.hal.NotifierJNI;
import org.wpilib.hardware.hal.RobotMode;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Teleop;
import org.wpilib.opmode.Utility;
import org.wpilib.system.RobotController;
import org.wpilib.system.Watchdog;
import org.wpilib.tunable.TunableRegistry;
import org.wpilib.util.Color;
import org.wpilib.util.ConstructorMatch;
import org.wpilib.util.UsageReporting;
import org.wpilib.util.WPIUtilJNI;

/**
 * LoggedOpModeRobot is the robot base class for a robot with separate OpMode classes. It is the
 * equivalent of WPILib's OpModeRobot and should be subclassed by the Robot class in the user
 * program.
 *
 * <p>As with all AdvantageKit robot base classes, custom periodic callbacks are not supported. See
 * the documentation for more details and recommended alternatives.
 *
 * <p>Classes annotated with {@link Autonomous}, {@link Teleop}, and {@link Utility} in the same
 * package or subpackages as the user's subclass are automatically registered as autonomous, teleop,
 * and utility OpModes respectively.
 *
 * <p>OpModes are constructed when selected on the driver station. While selected and disabled,
 * {@link OpMode#disabledPeriodic()} is called. When enabled, {@link OpMode#start()} is called once
 * and {@link OpMode#periodic()} runs at the rate from {@link #getPeriod()}. On disable or mode
 * switch while enabled, {@link OpMode#end()} is called and the OpModes is then closed and
 * discarded. When no OpModes is selected, {@link #nonePeriodic()} is called. {@link
 * #driverStationConnected()} is called once when the DS first connects.
 */
public abstract class LoggedOpModeRobot extends RobotBase {
  private final ControlWord word = new ControlWord();

  private record OpModeFactory(String name, Supplier<OpMode> supplier) {}

  private final Map<Long, OpModeFactory> opModes = new HashMap<>();

  // Callback system fields
  private final int notifier = NotifierJNI.createNotifier();
  private final long periodNs;
  private long nextCycleNs = 0;
  private boolean useTiming = true;

  // OpMode lifecycle state
  private long lastModeId = -1;
  private boolean calledDriverStationConnected = false;
  private boolean lastEnabledState = false;
  private OpMode currentOpMode;
  private String currentOpModeName;
  private boolean opModeRunning;
  private final Watchdog watchdog;

  private static void reportAddOpModeError(Class<?> cls, String message) {
    DriverStationErrors.reportError(
        "Error adding OpMode " + cls.getSimpleName() + ": " + message, false);
  }

  /**
   * Find a public constructor to instantiate the opmode. This constructor can have up to 2
   * parameters. The first parameter (if present) must be assignable from this.getClass(). The
   * second parameter (if present) must be assignable from DriverStationBase. If multiple, first
   * sort by most parameters, then by most specific first, then by most specific second.
   */
  private <T> Optional<ConstructorMatch<T>> findOpModeConstructor(Class<T> cls) {
    Optional<ConstructorMatch<T>> ctor;

    // try 1-parameter constructor with RobotBase parameter
    ctor = ConstructorMatch.findBestConstructor(cls, getClass());
    if (ctor.isPresent()) {
      return ctor;
    }

    // try no-parameter constructor
    ctor = ConstructorMatch.findBestConstructor(cls);
    return ctor;
  }

  private <T extends OpMode> T constructOpModeClass(Class<T> cls) {
    Optional<ConstructorMatch<T>> constructor = findOpModeConstructor(cls);
    if (constructor.isEmpty()) {
      DriverStationErrors.reportError(
          "No suitable constructor to instantiate OpMode " + cls.getSimpleName(), true);
      return null;
    }
    try {
      return constructor.get().newInstance(this);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(
          "Could not instantiate OpMode " + cls.getSimpleName(), e.getCause());
    }
  }

  private void checkOpModeClass(Class<?> cls) {
    // the class must be a subclass of OpMode
    if (!OpMode.class.isAssignableFrom(cls)) {
      throw new IllegalArgumentException("not a subclass of OpMode");
    }
    int modifiers = cls.getModifiers();
    // it cannot be abstract
    if (Modifier.isAbstract(modifiers)) {
      throw new IllegalArgumentException("is abstract");
    }
    // it must be public
    if (!Modifier.isPublic(modifiers)) {
      throw new IllegalArgumentException("not public");
    }
    // it must not be a non-static inner class
    if (cls.getEnclosingClass() != null && !Modifier.isStatic(modifiers)) {
      throw new IllegalArgumentException("is a non-static inner class");
    }
    // it must have a public no-arg constructor or a public constructor that accepts
    // this class
    // (or a superclass/interface) as an argument
    if (findOpModeConstructor(cls).isEmpty()) {
      throw new IllegalArgumentException(
          "missing public no-arg constructor or constructor accepting "
              + getClass().getSimpleName());
    }
  }

  /**
   * Adds an opmode using a factory function that creates the opmode. It's necessary to call
   * publishOpModes() to make the added mode visible to the driver station.
   *
   * @param mode robot mode
   * @param name name of the operating mode
   * @param group group of the operating mode
   * @param description description of the operating mode
   * @param textColor text color, or null for default
   * @param backgroundColor background color, or null for default
   * @param factory factory function to create the opmode
   * @throws IllegalArgumentException if class does not meet criteria
   */
  public void addOpMode(
      RobotMode mode,
      String name,
      String group,
      String description,
      Color textColor,
      Color backgroundColor,
      Supplier<OpMode> factory) {
    long id = RobotState.addOpMode(mode, name, group, description, textColor, backgroundColor);
    opModes.put(id, new OpModeFactory(name, factory));
  }

  /**
   * Adds an opmode using a factory function that creates the opmode. It's necessary to call
   * publishOpModes() to make the added mode visible to the driver station.
   *
   * @param mode robot mode
   * @param name name of the operating mode
   * @param group group of the operating mode
   * @param description description of the operating mode
   * @param factory factory function to create the opmode
   * @throws IllegalArgumentException if class does not meet criteria
   */
  public void addOpMode(
      RobotMode mode, String name, String group, String description, Supplier<OpMode> factory) {
    addOpMode(mode, name, group, description, null, null, factory);
  }

  /**
   * Adds an opmode using a factory function that creates the opmode. It's necessary to call
   * publishOpModes() to make the added mode visible to the driver station.
   *
   * @param mode robot mode
   * @param name name of the operating mode
   * @param group group of the operating mode
   * @param factory factory function to create the opmode
   * @throws IllegalArgumentException if class does not meet criteria
   */
  public void addOpMode(RobotMode mode, String name, String group, Supplier<OpMode> factory) {
    addOpMode(mode, name, group, "", factory);
  }

  /**
   * Adds an opmode using a factory function that creates the opmode. It's necessary to call
   * publishOpModes() to make the added mode visible to the driver station.
   *
   * @param mode robot mode
   * @param name name of the operating mode
   * @param factory factory function to create the opmode
   * @throws IllegalArgumentException if class does not meet criteria
   */
  public void addOpMode(RobotMode mode, String name, Supplier<OpMode> factory) {
    addOpMode(mode, name, "", factory);
  }

  /**
   * Adds an opmode for an opmode class. The class must be a public, non-abstract subclass of OpMode
   * with a public constructor that either takes no arguments or accepts a single argument
   * assignable from this robot class type (the latter is preferred; if multiple match, the most
   * specific parameter type is used). It's necessary to call publishOpModes() to make the added
   * mode visible to the driver station.
   *
   * @param mode robot mode
   * @param name name of the operating mode
   * @param group group of the operating mode
   * @param description description of the operating mode
   * @param textColor text color, or null for default
   * @param backgroundColor background color, or null for default
   * @param cls class to add
   * @throws IllegalArgumentException if class does not meet criteria
   */
  public void addOpMode(
      RobotMode mode,
      String name,
      String group,
      String description,
      Color textColor,
      Color backgroundColor,
      Class<? extends OpMode> cls) {
    checkOpModeClass(cls);
    addOpMode(
        mode,
        name,
        group,
        description,
        textColor,
        backgroundColor,
        () -> constructOpModeClass(cls));
  }

  /**
   * Adds an opmode for an opmode class. The class must be a public, non-abstract subclass of OpMode
   * with a public constructor that either takes no arguments or accepts a single argument
   * assignable from this robot class type (the latter is preferred; if multiple match, the most
   * specific parameter type is used). It's necessary to call publishOpModes() to make the added
   * mode visible to the driver station.
   *
   * @param mode robot mode
   * @param name name of the operating mode
   * @param group group of the operating mode
   * @param description description of the operating mode
   * @param cls class to add
   * @throws IllegalArgumentException if class does not meet criteria
   */
  public void addOpMode(
      RobotMode mode, String name, String group, String description, Class<? extends OpMode> cls) {
    addOpMode(mode, name, group, description, null, null, cls);
  }

  /**
   * Adds an opmode for an opmode class. The class must be a public, non-abstract subclass of OpMode
   * with a public constructor that either takes no arguments or accepts a single argument
   * assignable from this robot class type (the latter is preferred; if multiple match, the most
   * specific parameter type is used). It's necessary to call publishOpModes() to make the added
   * mode visible to the driver station.
   *
   * @param mode robot mode
   * @param name name of the operating mode
   * @param group group of the operating mode
   * @param cls class to add
   * @throws IllegalArgumentException if class does not meet criteria
   */
  public void addOpMode(RobotMode mode, String name, String group, Class<? extends OpMode> cls) {
    addOpMode(mode, name, group, "", cls);
  }

  /**
   * Adds an opmode for an opmode class. The class must be a public, non-abstract subclass of OpMode
   * with a public constructor that either takes no arguments or accepts a single argument
   * assignable from this robot class type (the latter is preferred; if multiple match, the most
   * specific parameter type is used). It's necessary to call publishOpModes() to make the added
   * mode visible to the driver station.
   *
   * @param mode robot mode
   * @param name name of the operating mode
   * @param cls class to add
   * @throws IllegalArgumentException if class does not meet criteria
   */
  public void addOpMode(RobotMode mode, String name, Class<? extends OpMode> cls) {
    addOpMode(mode, name, "", cls);
  }

  private void addOpModeClassImpl(
      Class<? extends OpMode> cls,
      RobotMode mode,
      String name,
      String group,
      String description,
      String textColor,
      String backgroundColor) {
    if (name == null || name.isBlank()) {
      name = cls.getSimpleName();
    }
    Color tColor = textColor.isBlank() ? null : Color.fromString(textColor);
    Color bColor = backgroundColor.isBlank() ? null : Color.fromString(backgroundColor);
    long id = RobotState.addOpMode(mode, name, group, description, tColor, bColor);
    opModes.put(id, new OpModeFactory(name, () -> constructOpModeClass(cls)));
  }

  private void addAnnotatedOpModeImpl(
      Class<? extends OpMode> cls, Autonomous auto, Teleop teleop, Utility utility) {
    checkOpModeClass(cls);

    // add an opmode for each annotation
    if (auto != null) {
      addOpModeClassImpl(
          cls,
          RobotMode.AUTONOMOUS,
          auto.name(),
          auto.group(),
          auto.description(),
          auto.textColor(),
          auto.backgroundColor());
    }
    if (teleop != null) {
      addOpModeClassImpl(
          cls,
          RobotMode.TELEOPERATED,
          teleop.name(),
          teleop.group(),
          teleop.description(),
          teleop.textColor(),
          teleop.backgroundColor());
    }
    if (utility != null) {
      addOpModeClassImpl(
          cls,
          RobotMode.UTILITY,
          utility.name(),
          utility.group(),
          utility.description(),
          utility.textColor(),
          utility.backgroundColor());
    }
  }

  /**
   * Adds an opmode for an opmode class annotated with {@link Autonomous}, {@link Teleop}, or {@link
   * Utility}. The class must be a public, non-abstract subclass of OpMode with a public constructor
   * that either takes no arguments or accepts a single argument assignable from this robot class
   * type (if multiple match, the most specific parameter type is used). It's necessary to call
   * publishOpModes() to make the added mode visible to the driver station.
   *
   * @param cls class to add
   * @throws IllegalArgumentException if class does not meet criteria
   */
  public void addAnnotatedOpMode(Class<? extends OpMode> cls) {
    Autonomous auto = cls.getAnnotation(Autonomous.class);
    Teleop teleop = cls.getAnnotation(Teleop.class);
    Utility utility = cls.getAnnotation(Utility.class);
    if (auto == null && teleop == null && utility == null) {
      throw new IllegalArgumentException("must be annotated with Autonomous, Teleop, or Utility");
    }
    addAnnotatedOpModeImpl(cls, auto, teleop, utility);
  }

  private void addAnnotatedOpModeClass(String className) {
    Class<? extends OpMode> cls;
    try {
      cls =
          Class.forName(className, false, Thread.currentThread().getContextClassLoader())
              .asSubclass(OpMode.class);
    } catch (ClassNotFoundException | ClassCastException e) {
      return;
    }
    Autonomous auto = cls.getAnnotation(Autonomous.class);
    Teleop teleop = cls.getAnnotation(Teleop.class);
    Utility utility = cls.getAnnotation(Utility.class);
    if (auto == null && teleop == null && utility == null) {
      return;
    }
    try {
      addAnnotatedOpModeImpl(cls, auto, teleop, utility);
    } catch (IllegalArgumentException e) {
      reportAddOpModeError(cls, e.getMessage());
    }
  }

  private void addAnnotatedOpModeClassesDir(
      File root, File dir, String packageName, Set<String> classNames) {
    File[] files = dir.listFiles();
    if (files == null) {
      return;
    }
    for (File file : files) {
      if (file.isDirectory()) {
        addAnnotatedOpModeClassesDir(root, file, packageName, classNames);
      } else if (file.getName().endsWith(".class")) {
        String relPath = root.toPath().relativize(file.toPath()).toString().replace('\\', '/');
        String relClassName = relPath.substring(0, relPath.length() - 6).replace('/', '.');
        classNames.add(packageName.isEmpty() ? relClassName : packageName + "." + relClassName);
      }
    }
  }

  /**
   * Scans for classes in the specified package and all nested packages that are annotated with
   * {@link Autonomous}, {@link Teleop}, or {@link Utility} and registers them. It's necessary to
   * call publishOpModes() to make the added modes visible to the driver station.
   *
   * @param pkg package to scan
   */
  public void addAnnotatedOpModeClasses(Package pkg) {
    String packageName = pkg.getName();
    String packagePath = packageName.replace('.', '/');
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Set<String> classNames = new TreeSet<>();

    try {
      Enumeration<URL> resources = classLoader.getResources(packagePath);
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        if ("jar".equals(resource.getProtocol())) {
          var connection = resource.openConnection();
          if (!(connection instanceof JarURLConnection jarConnection)) {
            DriverStationErrors.reportError(
                "Error scanning OpModes from "
                    + resource
                    + ": expected JarURLConnection, got "
                    + connection.getClass().getSimpleName(),
                false);
            continue;
          }
          jarConnection.setUseCaches(false);
          try (JarFile jar = jarConnection.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
              String name = entries.nextElement().getName();
              if (!name.startsWith(packagePath) || !name.endsWith(".class")) {
                continue;
              }
              String className = name.substring(0, name.length() - 6).replace('/', '.');
              classNames.add(className);
            }
          }
        } else if ("file".equals(resource.getProtocol())) {
          // Handle .class files in directories
          File dir = new File(resource.toURI());
          if (dir.exists() && dir.isDirectory()) {
            addAnnotatedOpModeClassesDir(dir, dir, packageName, classNames);
          }
        }
      }
    } catch (IOException | URISyntaxException e) {
      e.printStackTrace();
    }

    // Add all found classes in alphabetical order to ensure deterministic order
    // across platforms. This differs from the original WPILib behavior, but is
    // necessary to ensure that OpMode IDs are consistent across platforms in case
    // of a name hash conflict (since the resolution depends on registration order).
    for (String className : classNames) {
      addAnnotatedOpModeClass(className);
    }
  }

  /**
   * Removes an operating mode option. It's necessary to call publishOpModes() to make the removed
   * mode no longer visible to the driver station.
   *
   * @param mode robot mode
   * @param name name of the operating mode
   */
  public void removeOpMode(RobotMode mode, String name) {
    long id = RobotState.removeOpMode(mode, name);
    if (id != 0) {
      opModes.remove(id);
    }
  }

  /** Publishes the operating mode options to the driver station. */
  public void publishOpModes() {
    RobotState.publishOpModes();
  }

  /** Clears all operating mode options and publishes an empty list to the driver station. */
  public void clearOpModes() {
    RobotState.clearOpModes();
    opModes.clear();
  }

  /** Default loop period. */
  public static final double DEFAULT_PERIOD = 0.02;

  /** Constructor with default period. */
  @SuppressWarnings("this-escape")
  public LoggedOpModeRobot() {
    this(DEFAULT_PERIOD);
  }

  /**
   * Constructor with specified period.
   *
   * @param period the period at which to run the robot and opmode periodic callbacks.
   */
  @SuppressWarnings("this-escape")
  public LoggedOpModeRobot(double period) {
    // Create our own notifier and callback queue (match C++)
    NotifierJNI.setNotifierName(notifier, "LoggedOpModeRobot");
    this.periodNs = (long) (period * 1_000_000_000.0);

    watchdog = new Watchdog(Seconds.of(period), this::printLoopOverrunMessage);

    // Scan for annotated opmode classes within the derived class's package and
    // subpackages
    addAnnotatedOpModeClasses(getClass().getPackage());
    RobotState.publishOpModes();

    UsageReporting.reportUsage("Framework", "AdvantageKit_LoggedOpModeRobot");
    UsageReporting.reportUsage("LoggingFramework", "AdvantageKit");
  }

  /**
   * Get the period at which robot and opmode periodic callbacks are run.
   *
   * @return The period at which robot and opmode periodic callbacks are run.
   */
  public double getPeriod() {
    return (double) periodNs / 1_000_000_000.0;
  }

  /**
   * Code that needs to know the DS state should go here.
   *
   * <p>Users should override this method for initialization that needs to occur after the DS is
   * connected, such as needing the alliance information.
   */
  public void driverStationConnected() {}

  /** Function called periodically every loop, regardless of enabled state or OpMode selection. */
  public void robotPeriodic() {}

  /** Function called once during robot initialization in simulation. */
  public void simulationInit() {}

  /** Function called periodically in simulation. */
  public void simulationPeriodic() {}

  /** Function called once when the robot becomes disabled. */
  public void disabledInit() {}

  /** Function called periodically while the robot is disabled. */
  public void disabledPeriodic() {}

  /** Function called once when the robot exits disabled state. */
  public void disabledExit() {}

  /**
   * Function called periodically anytime when no opmode is selected, including when the Driver
   * Station is disconnected.
   */
  public void nonePeriodic() {}

  /**
   * Return the system clock time in nanoseconds for the start of the current loop cycle. This is
   * the same as Timer.getTimestamp() and Logger.getTimestamp(), but is stable through a loop. It is
   * updated at the beginning of every loop cycle.
   *
   * @return Robot running time in nanoseconds, as of the start of the current loop cycle.
   */
  public long getLoopStartTime() {
    return Logger.getTimestamp();
  }

  /** Main robot loop function. Handles disabled state logic and opmode management. */
  private void loopFunc() {
    DriverStationBackend.refreshData();

    // Get current enabled state and opmode
    DriverStationBackend.refreshControlWordFromCache(word);
    watchdog.reset();
    final boolean enabled = word.isEnabled();
    long modeId = word.isDSAttached() ? word.getOpModeId() : 0;

    boolean modeChanged = modeId != lastModeId;
    lastModeId = modeId;

    if (!calledDriverStationConnected && word.isDSAttached()) {
      calledDriverStationConnected = true;
      driverStationConnected();
      watchdog.addEpoch("driverStationConnected()");
    }

    // Handle opmode changes: tear down the old opmode if the selection changed
    if (modeChanged && currentOpMode != null) {
      endCurrentOpMode();
    }

    // Set up new opmode
    boolean justCreatedOpMode = false;
    if (modeId != 0 && currentOpMode == null && modeChanged) {
      OpModeFactory factory = opModes.get(modeId);
      if (factory != null) {
        currentOpModeName = factory.name();
        System.out.println("********** Creating OpMode " + currentOpModeName + " **********");
        currentOpMode = factory.supplier().get();
        if (currentOpMode != null) {
          // Warn about custom callbacks
          if (!currentOpMode.getCallbacks().isEmpty()) {
            DriverStationErrors.reportWarning(
                "OpMode "
                    + currentOpModeName
                    + " requested custom callbacks, which are incompatible with AdvantageKit. They will be ignored.",
                false);
          }

          // Call disabledPeriodic immediately for newly created OpMode
          currentOpMode.disabledPeriodic();
          watchdog.addEpoch("opMode.disabledPeriodic()");
          justCreatedOpMode = true;
        }
      } else {
        DriverStationErrors.reportError("No OpMode found for mode " + modeId, false);
      }
    }

    // Handle enabled state changes
    boolean justCalledDisabledInit = false;
    if (lastEnabledState != enabled) {
      if (enabled) {
        // Transitioning to enabled
        disabledExit();
        watchdog.addEpoch("disabledExit()");
      } else {
        // Transitioning to disabled. Only tear down an opmode that was actually
        // running; a freshly selected opmode entering its disabled phase must
        // persist so it can be started on the next enable.
        if (currentOpMode != null && opModeRunning) {
          endCurrentOpMode();
          lastModeId = -1; // force recreate next loop
        }
        disabledInit();
        watchdog.addEpoch("disabledInit()");
        justCalledDisabledInit = true;
      }
      lastEnabledState = enabled;
    }

    // Start the opmode if enabled and not already started. This single check
    // covers both the disabled->enabled transition and an opmode constructed
    // while the robot is already enabled.
    if (enabled && currentOpMode != null && !opModeRunning) {
      startCurrentOpMode();
    }

    // Call periodic functions based on current state
    if (!enabled) {
      // Only call disabledPeriodic if we didn't just call disabledInit
      if (!justCalledDisabledInit) {
        disabledPeriodic();
        watchdog.addEpoch("disabledPeriodic()");
      }

      // Call opmode disabledPeriodic if we have one
      if (currentOpMode != null && !justCreatedOpMode) {
        currentOpMode.disabledPeriodic();
        watchdog.addEpoch("opMode.disabledPeriodic()");
      }
    }

    // Call nonePeriodic when no opmode is selected
    if (modeId == 0) {
      nonePeriodic();
      watchdog.addEpoch("nonePeriodic()");
    }

    // Always call robotPeriodic
    robotPeriodic();
    watchdog.addEpoch("robotPeriodic()");

    // Call OpMode periodic
    if (enabled && currentOpMode != null && opModeRunning) {
      currentOpMode.periodic();
      watchdog.addEpoch("opMode.periodic()");
    }

    // Always observe user program state
    DriverStationJNI.observeUserProgram(word.getNative());

    TunableRegistry.update();
    watchdog.addEpoch("TunableRegistry.update()");

    // Call simulationPeriodic if in simulation
    if (isSimulation()) {
      HAL.simPeriodicBefore();
      simulationPeriodic();
      HAL.simPeriodicAfter();
      watchdog.addEpoch("simulationPeriodic()");
    }

    watchdog.disable();

    // Flush NetworkTables
    NetworkTableInstance.getDefault().flushLocal();

    // Warn on loop time overruns
    if (watchdog.isExpired()) {
      watchdog.printEpochs();
    }
  }

  private void startCurrentOpMode() {
    if (currentOpMode == null || opModeRunning) {
      return;
    }

    System.out.println("********** Starting OpMode " + currentOpModeName + " **********");
    opModeRunning = true;
    currentOpMode.start();
    watchdog.addEpoch("opMode.start()");
  }

  private void endCurrentOpMode() {
    if (opModeRunning) {
      System.out.println("********** Ending OpMode " + currentOpModeName + " **********");

      currentOpMode.end();
      watchdog.addEpoch("opMode.end()");
      opModeRunning = false;
    }

    System.out.println("********** Closing OpMode " + currentOpModeName + " **********");
    currentOpMode.close();
    currentOpMode = null;
    currentOpModeName = null;
  }

  /** Provide an alternate "main loop" via startCompetition(). */
  @Override
  public final void startCompetition() {
    try {
      // Robot init methods
      long initStart = RobotController.getMonotonicTime();
      if (isSimulation()) {
        simulationInit();
      }
      long initEnd = RobotController.getMonotonicTime(); // Includes Robot constructor and robotInit

      // Register auto logged outputs
      AutoLogOutputManager.addObject(this);

      // Save data from init cycle
      Logger.periodicAfterUser(initEnd - initStart, 0);

      // Tell the DS that the robot is ready to be enabled
      System.out.println("********** Robot program startup complete **********");
      DriverStationBackend.observeUserProgramStarting();

      // Loop foreve
      while (true) {
        if (useTiming) {
          long currentTimeNs = RobotController.getMonotonicTime();
          if (nextCycleNs < currentTimeNs) {
            // Loop overrun, start next cycle immediately
            nextCycleNs = currentTimeNs;
          } else {
            // Wait before next cycle
            NotifierJNI.setNotifierAlarm(notifier, nextCycleNs, 0, true, true);

            try {
              WPIUtilJNI.waitForObject(notifier);
            } catch (InterruptedException ex) {
              Logger.end();
              Thread.currentThread().interrupt();
              break;
            }
          }
          nextCycleNs += periodNs;
        }

        long periodicBeforeStart = RobotController.getMonotonicTime();
        Logger.periodicBeforeUser();
        long userCodeStart = RobotController.getMonotonicTime();
        loopFunc();
        long userCodeEnd = RobotController.getMonotonicTime();

        Logger.periodicAfterUser(userCodeEnd - userCodeStart, userCodeStart - periodicBeforeStart);
      }
    } catch (Exception exception) {
      StringWriter stringWriter = new StringWriter();
      exception.printStackTrace(new PrintWriter(stringWriter));
      Logger.periodicAfterUser(0, 0, stringWriter.toString());
      Logger.end();
      throw exception;
    }
  }

  /**
   * Sets whether to use standard timing or run as fast as possible.
   *
   * @param useTiming If true, use standard timing. If false, run as fast as possible.
   */
  public void setUseTiming(boolean useTiming) {
    this.useTiming = useTiming;
  }

  @Override
  public void close() {
    NotifierJNI.destroyNotifier(notifier);
    super.close();
  }

  /** Ends the main loop in startCompetition(). */
  @Override
  public final void endCompetition() {
    NotifierJNI.destroyNotifier(notifier);
  }

  private void printLoopOverrunMessage() {
    DriverStationErrors.reportWarning("Loop time of " + getPeriod() + "s overrun\n", false);
  }

  /** Prints list of epochs added so far and their times. */
  public void printWatchdogEpochs() {
    watchdog.printEpochs();
  }
}
