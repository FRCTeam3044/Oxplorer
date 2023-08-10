# Pathfinding 3044
This repo will be transferred to [FRCTeam3044](https://github.com/FRCTeam3044) when I am done working on it.

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
We ran the rio one last season and its slow as dirt, so we had to offload pathfinding to a processor. The server folder the (very broken) implimentation of the communication I used.
## ExampleProj
A (unfinished) example of implimenting pathfinding into a real robot project

## Support
Questions, Concerns, Suggestions, or anything of the sort? Feel free to message me on any of these:

- Discord: nab138
- Email: nab@nabdev.me
- ChiefDelphi: nab138

Or open a github issue/pr if that's more suitable.
