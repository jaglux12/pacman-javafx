/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx._3d.animation;

import static de.amr.games.pacman.lib.U.randomInt;

import java.util.Random;

import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.util.Vector3f;
import javafx.animation.Transition;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public abstract class SquirtingAnimation extends Transition {

	private static final Random RND = new Random();
	private static final Vector3f GRAVITY = new Vector3f(0, 0, 0.1f);

	private static float randomFloat(double left, double right) {
		float l = (float) left;
		float r = (float) right;
		return l + RND.nextFloat() * (r - l);
	}

	public static class Drop extends Sphere {

		private float vx;
		private float vy;
		private float vz;

		public Drop(double radius, PhongMaterial material, double x, double y, double z) {
			super(radius);
			setMaterial(material);
			setTranslateX(x);
			setTranslateY(y);
			setTranslateZ(z);
			setVisible(false);
		}

		public void setVelocity(float x, float y, float z) {
			vx = x;
			vy = y;
			vz = z;
		}

		public void move() {
			setTranslateX(getTranslateX() + vx);
			setTranslateY(getTranslateY() + vy);
			setTranslateZ(getTranslateZ() + vz);
			vx += GRAVITY.x();
			vy += GRAVITY.y();
			vz += GRAVITY.z();
		}
	}

	private final Group particleGroup = new Group();

	protected SquirtingAnimation(Group parent, double x, double y, double z) {
		var material = ResourceMgr.coloredMaterial(Color.gray(0.4, 0.25));
		for (int i = 0; i < randomInt(20, 40); ++i) {
			var drop = new Drop(randomFloat(0.1, 1.0), material, x, y, z);
			drop.setVelocity(randomFloat(-0.25, 0.25), randomFloat(-0.25, 0.25), -randomFloat(1.0, 4.0));
			particleGroup.getChildren().add(drop);
		}
		setCycleDuration(Duration.seconds(2));
		setOnFinished(e -> parent.getChildren().remove(particleGroup));
		parent.getChildren().add(particleGroup);
	}

	public abstract boolean reachesEndPosition(Drop drop);

	@Override
	protected void interpolate(double t) {
		for (var particle : particleGroup.getChildren()) {
			var drop = (Drop) particle;
			if (reachesEndPosition(drop)) {
				drop.setVelocity(0, 0, 0);
				drop.setScaleZ(0.1);
			} else {
				drop.setVisible(true);
				drop.move();
			}
		}
	}
}