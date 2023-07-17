# Pathfinding

Developed by [nab138](https://github.com/nab138)

Path generation for 3044 FRC Robotics using A-star w/ visibility graph and Bézier curve smoothing. 

This version is designed for running as a standalone server on a coprocessor. In the robot code, there is a seperate client for handling communication, and if the coprocessor is not found it will run on the rio (mostly used for sim)

This package hasn't been cleaned up since competition yet and will be packaged into a convienent gradle package and adjusted for modularity in the future. The name is probably goint to change. Additionally This is not the full extent of 3044's pathfinding system as much logic for when to use the pathfinder was in the actual robot code, but that logic will be left up to the team using this package. Path following was done with pure pursuit that may or may not be published in the future.