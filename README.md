# pacman-javafx

(WTF! I got >20 stars on a single day! How comes?)

A JavaFX user interface for my Pac-Man / Ms. Pac-Man game implementations. The game logic is implemented UI-independent, see repository [pacman-basic](https://github.com/armin-reichert/pacman-basic). Both games can be played in 2D and 3D scenes (work in progress).

The 3D model (unfortunately I have no animated model yet) has been generously provided by Gianmarco Cavallaccio (https://www.artstation.com/gianmart). Cudos to Gianmarco! 

In the current [release](https://github.com/armin-reichert/pacman-javafx/releases) you find an attached zip file containing the complete Java runtime needed to run the game. To start the game, unzip the archive and execute `bin\run.cmd`.

![Play Scene](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/playscene3D.png)

YouTube:

[![YouTube](https://github.com/armin-reichert/pacman-javafx/blob/main/pacman-ui-fx/doc/thumbnail.jpg)](https://www.youtube.com/watch?v=6ztHwLJuPNw&t=298s)

### Keys:

On the intro screen, you can switch between the two game variants by pressing <kbd>v</kbd>. You can switch between window and fullscreen mode using the standard keys <kbd>F11</kbd> and <kbd>Esc</kbd>.

On the play screen, the following functionality is available:
- <kbd>CTRL+C</kbd> Change 3D camera (3 cameras currently implemented)
- <kbd>CTRL+I</kbd> Toggle information about current scene (HUD)
- <kbd>CTRL+L</kbd> Toggle draw mode (line vs. shaded)
- <kbd>CTRL+P</kbd> Toggle pausing game play
- <kbd>CTRL+S</kbd> Increase speed
- <kbd>CTRL+SHIFT+S</kbd> Decrease speed
- <kbd>CTRL+3</kbd> Toggle using 2D/3D play scene
- <kbd>A</kbd> Toggle autopilot mode
- <kbd>I</kbd> Toggle immunity of player against ghost attacks
- <kbd>Q</kbd> Quit play scene
- Cheats:
  - <kbd>E</kbd> Eat all pills except the energizers
  - <kbd>L</kbd> Add one player life
  - <kbd>N</kbd> Enter next game level
  - <kbd>X</kbd> Kill all ghosts outside of the ghosthouse 
