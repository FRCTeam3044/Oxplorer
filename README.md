![Oxplorer](/assets/banner-light.png)
***The on-the-fly path planner for FRC***

[![Gradle Package](https://github.com/FRCTeam3044/Oxplorer/actions/workflows/build-release.yml/badge.svg)](https://github.com/FRCTeam3044/Oxplorer/actions/workflows/build-release.yml)

**Read the [FAQ](https://github.com/FRCTeam3044/Oxplorer/wiki/FAQ) and join the [3044 Packages Discord](https://discord.gg/ypRWZGnW66) for updates, support, discussion, and more!**

Oxplorer is a realtime, dynamic, on-the-fly path generator designed for use with FRC robots. It can avoid any obstacles on a given field map and generate the optimal path in an instant.

Oxplorer is not finished, and will continue to be updated (For those worried it is too slow, I have several optimiziations implemented that just need some polishing and bug-fixing before a merge). You may have to play with the path settings a little to get them to look right. If there is a feature you want, please feel free to let me know!

For more info, you can read the [wiki](https://github.com/FRCTeam3044/Oxplorer/wiki) and you can explore the [javadoc](https://frcteam3044.github.io/Oxplorer/)!

Take a look at the [Roadmap](https://trello.com/b/DJ243CXC/oxplorer) for an idea of whats to come.

## Installation

Add the folowing to the repositories section of your `build.gradle`:

```gradle
maven {
  url = uri("https://maven.pkg.github.com/FRCTeam3044/Oxplorer")
  credentials {
      username = "3044-Packages-Bot"
      password = "\u0067\u0068\u0070\u005f\u0038\u0055\u0068\u0037\u0061\u004f\u0062\u0049\u004a\u0041\u005a\u0045\u0059\u0073\u0041\u0055\u0033\u0063\u0041\u0037\u004f\u0065\u0070\u0037\u0053\u0074\u0073\u0058\u0058\u0059\u0031\u004e\u006e\u0056\u0030\u004a"
  }
}
```

Then add this line to the dependencies section:

```gradle
implementation 'me.nabdev.pathfinding:oxplorer:0.12.6'
```

## Usage

Here's a basic example of usage (see more options for the builder on it's [javadoc page](https://frcteam3044.github.io/Oxplorer/me/nabdev/pathfinding/PathfinderBuilder.html))

```java
Pathfinder pathfinder = new PathfinderBuilder(Field.CRESCENDO_2024).build();

try {
  TrajectoryConfig config = new TrajectoryConfig(3 /* Max vel */, 3 /* Max accel */);
  Trajectory myPath = pathfinder.generateTrajectory(m_robotDrive.getPose(), new Pose2d(8, 4, new Rotation2d()), config);
} catch (ImpossiblePathException e) {
  e.printStackTrace();
}
```

## Support

Questions, Concerns, Suggestions, Bug reports, or anything of the sort? Feel free to reach out on any of these:

- Discord: [3044 Packages Discord](https://discord.gg/ypRWZGnW66)
- Email: nab@nabdev.me
- ChiefDelphi: nab138

Or open a github issue/pr if that's more suitable.

## Links

- Developer: [nab138](https://github.com/nab138)
- [JSON-java](https://github.com/stleary/JSON-java)
- [allwpilib](https://github.com/wpilibsuite/allwpilib)
- [REV Lib](https://docs.revrobotics.com/sparkmax/software-resources/spark-max-api-information)
