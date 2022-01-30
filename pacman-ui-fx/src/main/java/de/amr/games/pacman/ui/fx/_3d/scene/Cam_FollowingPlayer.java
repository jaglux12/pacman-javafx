/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._3d.scene;

import static de.amr.games.pacman.ui.fx.util.Animations.lerp;

import javafx.scene.transform.Rotate;

/**
 * Follows the player but still shows a larger portion of the maze.
 * 
 * @author Armin Reichert
 */
public class Cam_FollowingPlayer extends CameraController {

	@Override
	public void reset() {
		perspectiveCam.setNearClip(0.1);
		perspectiveCam.setFarClip(10000.0);
		perspectiveCam.setRotationAxis(Rotate.X_AXIS);
		perspectiveCam.setRotate(60);
		perspectiveCam.setTranslateZ(-160);
	}

	@Override
	public void update(PlayScene3D scene) {
		// TODO this is just trial and error
		double frac = 0.03;
		double x = lerp(perspectiveCam.getTranslateX(), scene.player3D.getTranslateX() - 100, frac);
		double y = lerp(perspectiveCam.getTranslateY(), scene.player3D.getTranslateY() + 60, frac);
		perspectiveCam.setTranslateX(x);
		perspectiveCam.setTranslateY(y);
	}

	@Override
	public String toString() {
		return "Following Player";
	}
}