/*
MIT License

Copyright (c) 2022 Armin Reichert

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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.actors.Creature;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Base class for 3D creatures (Pac-Man, ghosts) that move through the maze and turn around corners.
 * 
 * @author Armin Reichert
 */
public abstract class MovingCreature3D<C extends Creature> extends Group {

	public record Turn(int fromAngle, int toAngle) {
	}

	private static final int L = 0;
	private static final int U = 90;
	private static final int R = 180;
	private static final int D = 270;

	private static double getAngle(Direction dir) {
		return switch (dir) {
		case LEFT -> L;
		case RIGHT -> R;
		case UP -> U;
		case DOWN -> D;
		default -> L;
		};
	}

	//@formatter:off
	private static final Turn[][] TURNS = {
		{ null,            new Turn(L, R), new Turn(L, U), new Turn(L, -U) }, // LEFT -> *
		{ new Turn(R, L),  null,           new Turn(R, U), new Turn(R, D)  }, // RIGHT -> *
		{ new Turn(U, L),  new Turn(U, R), null,           new Turn(U, D)  }, // UP -> *
		{ new Turn(-U, L), new Turn(D, R), new Turn(-U, U), null           }, // DOWN -> *
	};
	//@formatter:on

	private static int index(Direction dir) {
		return dir == null ? 0 : dir.ordinal();
	}

	protected final C guy;
	protected Direction animationTargetDir;
	private RotateTransition turnAnimation;

	protected MovingCreature3D(C guy) {
		this.guy = guy;
		setTranslateZ(-HTS);
		turnAnimation = new RotateTransition(Duration.seconds(0.25), this);
		turnAnimation.setAxis(Rotate.Z_AXIS);
		turnAnimation.setInterpolator(Interpolator.EASE_BOTH);
		resetMovement();
	}

	protected void resetMovement() {
		animationTargetDir = guy.moveDir();
		turnAnimation.stop();
		setRotationAxis(Rotate.Z_AXIS);
		setRotate(getAngle(animationTargetDir));
	}

	protected void updateMovement() {
		setTranslateX(guy.position().x() + HTS);
		setTranslateY(guy.position().y() + HTS);
		if (animationTargetDir != guy.moveDir()) {
			var turn = TURNS[index(animationTargetDir)][index(guy.moveDir())];
			turnAnimation.stop();
			turnAnimation.setFromAngle(turn.fromAngle);
			turnAnimation.setToAngle(turn.toAngle);
			turnAnimation.playFromStart();
			animationTargetDir = guy.moveDir();
		}
	}
}