# Getting Started: Developing AdvantageKit

We use [Bazel](https://bazel.build/) to build, run, and test AdvantageKit code.  To start working on AdvantageKit, you must first install it.

If you've done this kind of thing before, you can follow [Bazel's official installation instructions](https://docs.bazel.build/versions/main/install.html), install a [supported version of the JDK](https://adoptium.net/?variant=openjdk11&jvmVariant=hotspot), and install a supported C++ compiler suite for your platform.
> Please note if you're developing on Windows, you'll need to set up your user.bazelrc file.  See the last step in that section to get going.

If you don't know where to start with any of these, follow one of our guides below for your operating system.

## Windows

Make sure you have the JDK installed (preferably version 11).  A supported version of JDK 11 can be downloaded from [here](https://adoptium.net/?variant=openjdk11&jvmVariant=hotspot).
> If you already have JDK 11 installed, or wish to install it a different way, you can skip this step.

Bazel on Windows can be installed a in variety of ways.  We recommend installing it using Chocolatey using [these instructions](https://docs.bazel.build/versions/main/install-windows.html#using-chocolatey).

If you need to build C++ code that runs on Windows (not needed for most components of AdvantageKit), you will also need to install either GCC under Mingw/MSYS2 (not documented here) or [Visual Studio Build Tools](https://visualstudio.microsoft.com/downloads/?q=build+tools#build-tools-for-visual-studio-2022).
> Note that MSVC (Visual Studio Compiler) is not officially supported by us, but we will do our best to ensure we are writing code that works on any standards-compliant C++ compiler.

You will need to create a file called `user.bazelrc` in the project root directory with the following content:
> The settings in this file are needed to ensure there are no folders with spaces in their names on the path that Bazel uses to build, and that the command length to run the compiler does not exceed Windows' (very short) path length limit.
```
startup --output_user_root=C:\\bazelroot
build --features=compiler_param_file
```

## Linux

>These instructions are written assuming you are using Ubuntu/Debian or Ubuntu/Debian on WSL2, on a 64-bit machine.  Other distros should work but may require different commands for installing packages.  If you're using another distro, use whatever package management tools you are familiar with.  If using another CPU architecture, make the appropriate substitution when downloading files described below.

Update apt cache and install git, build-essential (C++ build tools), and the JDK (and sources):
```console
sudo apt update
sudo apt install git build-essential openjdk-11-jdk openjdk-11-source
```

Download Bazel by downloading the `bazel_4.1.0-linux-x86_64.deb` file from [here](https://github.com/bazelbuild/bazel/releases/tag/4.1.0).

From the command line, this file can be downloaded by running this command:
```console
wget https://github.com/bazelbuild/bazel/releases/download/4.1.0/bazel_4.1.0-linux-x86_64.deb
```

Next, install the downloaded package.  From the command line:
```console
sudo apt install ./bazel_4.1.0-linux-x86_64.deb
```

## macOS

As with Windows, make sure you have the JDK installed (preferably version 11). A supported version of JDK 11 can be downloaded from [here](https://adoptium.net/?variant=openjdk11&jvmVariant=hotspot).
> If you already have JDK 11 installed, or wish to install it a different way, you can skip this step.

Bazel can be installed in multiple ways on macOS. We recommend using [these instructions](https://docs.bazel.build/versions/main/install-os-x.html#install-with-installer-mac-os-x) to set up Bazel as a standalone install. For users on Apple Silicon (ARM) based Macs, the `arm64` version of Bazel is fully supported.

Alternatively, if you've already set up [Homebrew](https://docs.bazel.build/versions/main/install-os-x.html#install-with-installer-mac-os-x), the Bazel package can be installed using the following command:
```
brew install bazel
```