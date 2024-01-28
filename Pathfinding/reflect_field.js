const fs = require("fs");

let field = JSON.parse(fs.readFileSync("./crescendo_2024.json"), "utf8");
let obstacles = field.obstacles;

let reflectectedObstacles = [];

let fieldWidth = 16.55;

for (let obstacle of obstacles) {
  let newObstacle = {
    ...obstacle,
  };
  newObstacle.vertices = newObstacle.vertices.reverse();
  for (let vertex of newObstacle.vertices) {
    vertex[0] = fieldWidth - vertex[0];
  }
  reflectectedObstacles.push(newObstacle);
}

field.obstacles = reflectectedObstacles;

fs.writeFileSync(
  "./crescendo_2024_reflected.json",
  JSON.stringify(field, null, 2)
);
