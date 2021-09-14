# AdvantageKit

## Getting Started

### Windows

We **strongly** recommend using WSL2 (or WSL1 as a fallback) to develop AdvantageKit on Windows.  While we do our best to support Windows as a build environment, many Bazel features do not work well on Windows and managing C/C++ compilers on Windows is quite tedious.  Microsoft provides detailed instructions on setting up WSL2, and it is natively supported in VSCode as well.

[WSL2 Setup Guide](https://docs.microsoft.com/en-us/windows/wsl/install-win10#manual-installation-steps)

After setting up WSL2 (we recommend Ubuntu as a distro), follow the [Linux](#linux) instructions below to complete the setup.

> If you can't use WSL2 because your computer doesn't support virtualization, WSL1 (sometimes just called WSL) should still work for building AdvantageKit (though it is untested).  Follow the instructions in the guide, but skip step 5.

If not using WSL2/WSL1, you will need to create a file called `user.bazelrc` in the project root directory with the following content:
```
startup --output_user_root=C:\\bazelroot
```
Note that you will also need to have Administrator privileges to build on Windows, and you will need to have [Visual Studio Build Tools](https://visualstudio.microsoft.com/downloads/?q=build+tools#build-tools-for-visual-studio-2019) installed (note that we only officially support GCC as a compiler so some code may not build under MSVC), or run within [MinGW](https://www.mingw-w64.org/)/[MSYS2](https://www.msys2.org/) with GCC installed to build any C/C++ code targeting platforms other than the roboRIO.
> Compiling C/C++ code exclusively for the roboRIO should be fully supported in Windows without the above components assuming you set up your `user.bazelrc` correctly.

Finally, Bazel itself must be installed.  We recommend installing it using Chocolatey using [these instructions](https://docs.bazel.build/versions/main/install-windows.html#using-chocolatey).

### Linux

>These instructions are written assuming you are using Ubuntu or Ubuntu on WSL2, on a 64 bit machine.  Other distros should work, but may require different commands for installing packages.  If you're using another distro, use whatever package management tools you are familiar with.  If using another CPU architecture, make the appropriate substitution when downloading files described below.

This procedure will use the command line to install Bazel.  If you don't have time to read, just execute each of the commands listed in the boxes below to get up and running.

We recommend making a directory where you can download and extract the files listed below without making a mess in your home folder:
```console
cd ~/
mdkir bazel_install
cd bazel_install
```

Install GCC and Java:
```console
sudo apt install build-essential openjdk-11-jdk
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

Install FUSE and sandboxfs, which Bazel requires to work properly.  Detailed instructions are available [here](https://github.com/bazelbuild/sandboxfs/blob/master/INSTALL.md#using-the-generic-linux-pre-built-binaries), but for 64-bit Ubuntu we provide them below:
```console
sudo apt install libfuse2
```
```console
wget https://github.com/bazelbuild/sandboxfs/releases/download/sandboxfs-0.2.0/sandboxfs-0.2.0-20200420-linux-x86_64.tgz
```
```console
sudo tar xzv -C /usr/local -f sandboxfs-0.2.0-20200420-linux-x86_64.tgz
```

Bazel should now be ready to use.  To clean up your installation files, run:
```console
cd ..
rm -rf bazel_install
```

