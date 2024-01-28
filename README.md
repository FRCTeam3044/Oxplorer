# Oxplorer - The on-the-fly pathfinder for FRC!

**Join the [3044 Packages Discord](https://discord.gg/ypRWZGnW66) for updates, support, discussion, and more!**

Oxplorer is a realtime, dynamic, on-the-fly path generator designed for use with FRC robots. It can avoid any obstacles on a given field map and generate the optimal path in an instant.

A wiki is on it's way, but in the meantime, you can explore the [javadoc](https://frcteam3044.github.io/Oxplorer/)!

Take a look at the [Roadmap](https://trello.com/b/DJ243CXC/oxplorer) for and idea of whats to come.

## Installation

Add the folowing to the repositories section of your `build.gradle`:

```gradle
maven {
  url = uri("https://maven.pkg.github.com/FRCTeam3044/Oxplorer")
  credentials {
          username = "Mechanical-Advantage-Bot"
          password = "\u0067\u0068\u0070\u005f\u006e\u0056\u0051\u006a\u0055\u004f\u004c\u0061\u0079\u0066\u006e\u0078\u006e\u0037\u0051\u0049\u0054\u0042\u0032\u004c\u004a\u006d\u0055\u0070\u0073\u0031\u006d\u0037\u004c\u005a\u0030\u0076\u0062\u0070\u0063\u0051"
  }
}
```

Then add this line to the dependencies section:
```gradle
implementation 'me.nabdev.pathfinding:oxplorer:0.7.0'
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

## ExampleProj

A (unfinished) example of implimenting pathfinding into a real robot project

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