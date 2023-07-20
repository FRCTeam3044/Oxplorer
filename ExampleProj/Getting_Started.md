# Getting Started

## Code Setup

1. Install the newest [WPILib release](https://github.com/wpilibsuite/allwpilib/releases) for your platform.
2. Install the [REV client](https://docs.revrobotics.com/rev-hardware-client/) to interact with the Spark MAX motor
   controllers.
3. Open `2024-Everybot\Java\Rio` in VSCode to get proper tooling support
4. Navigate to `.wpilib\wpilib_preferences.json` and change the team number to match your own.
5. To run a test build either click the wpilib logo and search for "build robot code" or in your terminal type
   `./gradlew build`
6. To start using the simulation either again search for "Simulate Robot Code" or type in your terminal
   `./gradlew simulateJava`
7. To deploy to the actual robot either again search for "Deploy Robot Code" or type in your terminal
   `./gradlew deploy`

For immediate testing as soon as you have the code base downloaded you can run `./gradlew simulateJava` to start the
simGUI to get a feel for driving, and start developing code while your everybot is being assembled.

## Hardware Setup

1. Flash your radio with the newest firmware, instructions can be found
   [here](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-3/radio-programming.html)
2. Set your Spark Max CAN IDs using the [REV client](https://docs.revrobotics.com/rev-hardware-client/) to match the
   following table:

| Motor       | ID |
|-------------|----|
| Left Front  | 11 |
| Left Back   | 12 |
| Right Front | 13 |
| Right Back  | 14 |

3. Confirm your Rio is connected to the radio, you can connect to the robot with the DS, & the CAN network is fully
   intact, from here you're ready to start deploying the base everybot code by running `./gradlew deploy` in your
   terminal.

### Resources

- [WPILib Documentation](https://docs.wpilib.org/en/stable/)
- [REV Robotics Documentation](https://docs.revrobotics.com/)
- [CTRE Documentation](https://phoenix-documentation.readthedocs.io/en/latest/index.html)
- [Introduction to Java Programming Princeton](https://introcs.cs.princeton.edu/java/home/)
- [Introduction to Java Video](https://www.youtube.com/watch?v=A74TOX803D0&t)
- [Control Theory for FRC](https://controls-in-frc.link/)
- Intro to Mathematics [3 Blue 1 Brown](https://www.youtube.com/@3blue1brown):
    - [Geometry](https://www.youtube.com/watch?v=GNcFjFmqEc8&list=PLZHQObOWTQDMXMi3bUMThGdYqos36X_lA)
    - [Statistics](https://www.youtube.com/watch?v=8idr1WZ1A7Q&list=PL0t49HdSsmyZHmM96zyRCf79kSBnb9RRH)
    - [Linear Algebra](https://www.youtube.com/watch?v=kjBOesZCoqc&list=PL0-GT3co4r2y2YErbmuJw2L5tW4Ew2O5B)
    - [Calculus](https://www.youtube.com/watch?v=WUvTyaaNkzM&list=PLZHQObOWTQDMsr9K-rj53DwVRMYO3t5Yr)
- [Photon Vision](https://docs.photonvision.org/en/latest/)

### Using Vendordeps

Vendordeps are WPIlib's way of managing Java & C++ dependencies in an easy json format, most common vendordeps can be
found
at [WPILib Third Party Libraries](https://docs.wpilib.org/en/stable/docs/software/vscode-overview/3rd-party-libraries.html)
to install a vendordep there are two ways:

1. Open the wpilib tool menu in the top right corner if using VSCode, select "Manage Vendor Libraries", and then
   "Install new libraries (online)". Then paste in the vendordep URL for your desired library.
2. Copy the raw vendordep json file into Rio/vendordeps, to get this json if you do not have it already, you can paste
   the vendordep URL into your browser and copy the raw json.

### Using Gradle

Gradle is used to build, deploy, simulate, and more, it is the backend tooling for WPILib code bases, in most cases you
will not need to touch `settings.gradle` or `build.gradle` but if you need to add a dependency that does not have a
vendor dep you can add it to `build.gradle` under dependencies to install a maven package.

### Using Git

Git & Specifically GitHub are used to manage code changes and versions, if you are not familiar with git, you can learn
more at [Git Handbook](https://guides.github.com/introduction/git-handbook/). We provide an included `.gitignore` file
which prevents certain unnecessary files from being tracked by git, if you need to add a file to git that is being
ignored you can remove it or the folder it is in from the `.gitignore` file. To add a file to `.gitignore` you can
add a single file `example.txt`, a folder `/example/`, or a file type `*.txt`. To users new to git using the GitHub
desktop client is recommended, it can be downloaded at [GitHub Desktop](https://desktop.github.com/).

#### GitHub Actions

This repository contains a GitHub Actions workflow that will automatically build and test the code on every push to
confirm that the code compiles and passes all tests. This is a great way to ensure that the code is always in a working
state. The workflow can be found in `.github/workflows/build.yml`. The workflow is configured to run on every push to
the `main` branch. If you would like to change this, you can edit the `on` section of the workflow file. The workflow
will also run on every pull request to the `main` branch. If you would like to change this, you can edit the
`pull_request` section of the workflow file.