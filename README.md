# Pathfinding 3044
This repo will be transferred to [FRCTeam3044](https://github.com/FRCTeam3044) when I am done working on it. It's only here for convenience right now. I have activley been working on it these past couple weeks, but I broke a few things so I haven't committed here yet.

## Pathfinding
The pathfinding folder contains the actual pathfinding code. It's still being worked on, so things like obstacle inflation and line intersection have some issues I'm aware of. I'll try to fix it asap.

Here's a basic example of usage (note that this probably will change in the future):

```java
Pathfinder pathfinder = new Pathfinder(Field.CHARGED_UP_2023);

try {  Path path = pathfinder.generatePath(new Vertex(1, 1), new Vertex(8, 4));
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
