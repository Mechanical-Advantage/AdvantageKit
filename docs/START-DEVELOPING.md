# Getting Started: Developing AdvantageKit

We use [Bazel](https://bazel.build/) to build, run, and test AdvantageKit code.  To start working on AdvantageKit, you must first install it.

**IMPORTANT: Bazel 5.x.x is not yet supported by the VSCode Bazel Java plugin.  While everything else should work fine, we recommend staying with Bazel 4.x.x until this is resolved if this will affect you.**

If you've done this kind of thing before, you can follow [Bazel's official installation instructions](https://docs.bazel.build/versions/main/install.html), install a [supported version of the JDK](https://adoptium.net/?variant=openjdk11&jvmVariant=hotspot), and install a supported C++ compiler suite for your platform.
> Please note if you're developing on Windows, you'll need to set up your user.bazelrc file.  See the last step in that section to get going.

If you don't know where to start with any of these, follow one of our guides below for [Windows](#windows), [Linux](#linux), or [macOS](#macos).

## Windows

**Many developer convenience features are not supported on Windows.  If you are interested in doing extensive development work on AdvantageKit, we strongly recommend setting up [WSL/WSL2](https://docs.microsoft.com/en-us/windows/wsl/install) and working within that environment.  WSL has excellent integration with VSCode and is fully supported by us as a stable development environment.**

If you choose to use WSL2, please follow the instructions for [Linux](#linux).  Otherwise, proceed with the below instructions.

Make sure you have JDK 11 or higher installed.  A supported version of JDK 11 can be downloaded from [here](https://adoptium.net/?variant=openjdk11&jvmVariant=hotspot).
> If you already have JDK 11 installed, or wish to install it a different way, you can skip this step.  You can check your version of the JDK by running "java -version" in a command prompt.

Bazel on Windows can be installed a in variety of ways.  We recommend installing it with [Chocolatey](https://chocolatey.org/install#install-step2) using [these instructions](https://docs.bazel.build/versions/main/install-windows.html#using-chocolatey).

If you need to build C++ code that runs on Windows (needed for building Conduit), you will need to install [Visual Studio Build Tools](https://download.visualstudio.microsoft.com/download/pr/33439e30-02ff-417f-b6ef-927e424e84c9/cff2eb1a766df58fa77a9a89e7fc1765cc419c3175cd26bb0c97806649ab0981/vs_BuildTools.exe).

> Please note that VS Build Tools 2022 is NOT supported by Bazel.  Our above link goes directly to the download link for the VS Build Tools 2019 x86_64, the latest supported version.

>If you experience errors about a missing toolchain, which can happen when you have multiple versions of Visual Studio installed, you may need to set the [BAZEL_VC](https://docs.bazel.build/versions/main/windows.html#build-c-with-msvc) envirionment variable.

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

Bazel can be installed in multiple ways on macOS. We recommend using [these instructions](https://docs.bazel.build/versions/main/install-os-x.html#install-with-installer-mac-os-x) to set up Bazel as a standalone install. 

>For users on Apple Silicon (ARM) based Macs, please continue to use the 'x86_64' version of Bazel, as several of our precompiled libraries are not yet targeting arm64.  Using the x86_64 build of Bazel ensures that an x86_64 C++ toolchain is selected.

Alternatively, if you've already set up [Homebrew](https://docs.bazel.build/versions/main/install-os-x.html#install-with-installer-mac-os-x), the Bazel package can be installed using the following command:
```
brew install bazel
```

>If you are on an Apple Silicon Mac, please do not use Homebrew to install Bazel and instead use the above instructions to ensure an x86_64 version of Bazel is installed.