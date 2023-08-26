# Oxplorer by [nab138](https://github.com/nab138)

**Join the [3044 Packages Discord](https://discord.gg/ypRWZGnW66) for updates, support, discussion, and more!**

I will hopefully make a wiki and publish this as a package soon, but for now it is fully javadoced if you're interested in using it.

## Pathfinding

The pathfinding folder contains the actual pathfinding code. I've mostly cleaned a lot of this up and documented it, so if you find any issues (bugs, conceptual, potential optimizations, etc) please let me know.

Here's a basic example of usage:

```java
Pathfinder pathfinder = new PathfinderBuilder(Field.CHARGED_UP_2023).build();

try {
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
