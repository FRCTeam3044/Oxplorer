# Oxplorer - The on-the-fly pathfinder for FRC!

**Join the [3044 Packages Discord](https://discord.gg/ypRWZGnW66) for updates, support, discussion, and more!**

Oxplorer is a realtime, dynamic, on-the-fly path generator designed for use with FRC robots. It can avoid any obstacles on a given field map, avoid dynamic obstacles if you have a detector of your own, and more.
Oxplorer is *not* a path following library. It can generate paths, but not follow them. That is up to you (I do have a unreleased method that converts my path structure to a WPILib trajectory that might work for some people, it will be released soon).

Hopefully a wiki is coming soon, but all the methods have full javadoc if you want to play with it.

Take a look at the [Roadmap](https://trello.com/b/DJ243CXC/oxplorer)

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
implementation 'me.nabdev.pathfinding:oxplorer:0.4.1'
```

## Usage

Here's a basic example of usage (there are plenty of configuration options on the builder, while I am working on documentation you can use your IDE to see each available method and its javadoc):

```java
Pathfinder pathfinder = new PathfinderBuilder(Field.CHARGED_UP_2023).build();

try {
  // You can also just use Pose2ds
  Path path = pathfinder.generatePath(new Vertex(1, 1), new Vertex(8, 4));
} catch (ImpossiblePathException e) {
  e.printStackTrace();
}
```

## Server

The Server folder has the semi-broken code used to offload pathfinding to a processor. Hopefully with all the recent optimizations this won't be neccesary anymore.

## ExampleProj

A (unfinished) example of implimenting pathfinding into a real robot project

## Support

Questions, Concerns, Suggestions, Bug reports, or anything of the sort? Feel free to reach out on any of these:

- Discord: [3044 Packages Discord](https://discord.gg/ypRWZGnW66)
- Email: nab@nabdev.me
- ChiefDelphi: nab138

Or open a github issue/pr if that's more suitable.

### Credits
Developer - [nab138](https://github.com/nab138)
[WPILIB](https://github.com/wpilibsuite)
