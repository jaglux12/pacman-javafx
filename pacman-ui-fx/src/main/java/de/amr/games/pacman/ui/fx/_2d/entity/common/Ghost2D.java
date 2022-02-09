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
package de.amr.games.pacman.ui.fx._2d.entity.common;

import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost2D {

	public final Ghost ghost;
	public final Rendering2D r2D;

	public Map<Direction, TimedSeq<Rectangle2D>> kickings;
	public Map<Direction, TimedSeq<Rectangle2D>> returningHomeAnimations;
	public TimedSeq<Rectangle2D> flashingAnimation;
	public TimedSeq<Rectangle2D> frightenedAnimation;
	private boolean looksFrightened;

	public Ghost2D(Ghost ghost, Rendering2D r2D) {
		this.ghost = ghost;
		this.r2D = r2D;
		reset();
	}

	public void reset() {
		kickings = r2D.createGhostKickingAnimations(ghost.id);
		returningHomeAnimations = r2D.createGhostReturningHomeAnimations();
		frightenedAnimation = r2D.createGhostFrightenedAnimation();
		flashingAnimation = r2D.createGhostFlashingAnimation();
	}

	public void setLooksFrightened(boolean looksFrightened) {
		this.looksFrightened = looksFrightened;
	}

	public void render(GraphicsContext g) {
		Rectangle2D sprite = null;
		if (ghost.bounty > 0) {
			sprite = r2D.getBountyNumberSprite(ghost.bounty);
		} else if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			sprite = returningHomeAnimations.get(ghost.wishDir()).animate();
		} else if (ghost.is(FRIGHTENED)) {
			if (flashingAnimation.isRunning()) {
				sprite = flashingAnimation.animate();
			} else {
				if (ghost.velocity.equals(V2d.NULL)) {
					sprite = frightenedAnimation.frame();
				} else {
					sprite = frightenedAnimation.animate();
				}
			}
		} else if (ghost.is(LOCKED) && looksFrightened) {
			sprite = frightenedAnimation.animate();
		} else if (ghost.velocity.equals(V2d.NULL)) {
			sprite = kickings.get(ghost.wishDir()).frame();
		} else {
			sprite = kickings.get(ghost.wishDir()).animate();
		}
		r2D.renderEntity(g, ghost, sprite);
	}
}