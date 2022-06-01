/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ISpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimationMap;
import javafx.scene.canvas.GraphicsContext;

/**
 * 2D representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost2D extends GameEntity2D {

	public enum AnimationKey {
		KICKING, EYES, NUMBER, FRIGHTENED, FLASHING;
	};

	public final Ghost ghost;
	private AnimationKey selected;

	public SpriteAnimationMap<Direction> animEyes;
	public SpriteAnimation animFlashing;
	public SpriteAnimation animFrightened;
	public SpriteAnimationMap<Direction> animKicking;
	public SpriteAnimation animNumber;

	public Ghost2D(Ghost ghost, GameModel game, Rendering2D r2D) {
		super(game);
		this.ghost = ghost;
		animEyes = r2D.createGhostReturningHomeAnimations();
		animFlashing = r2D.createGhostFlashingAnimation();
		long flashTicks = TickTimer.sec_to_ticks(2) / (game.level.numFlashes * animFlashing.numFrames());
		animFlashing.frameDuration(flashTicks).repetitions(game.level.numFlashes);
		animFrightened = r2D.createGhostFrightenedAnimation();
		animKicking = r2D.createGhostKickingAnimations(ghost.id);
		animNumber = SpriteAnimation.of(r2D.getBountyNumberSprite(200), r2D.getBountyNumberSprite(400),
				r2D.getBountyNumberSprite(800), r2D.getBountyNumberSprite(1600));

		selectAnimation(AnimationKey.KICKING);
	}

	private ISpriteAnimation selectedAnimation() {
		return switch (selected) {
		case EYES -> animEyes;
		case FLASHING -> animFlashing;
		case FRIGHTENED -> animFrightened;
		case KICKING -> animKicking;
		case NUMBER -> animNumber;
		};
	}

	public void selectAnimation(AnimationKey key) {
		this.selected = key;
		selectedAnimation().ensureRunning();
	}

	public void resetAnimations() {
		Stream.of(animEyes, animFlashing, animFrightened, animKicking, animNumber).forEach(ISpriteAnimation::reset);
	}

	public void startFlashing(int numFlashes, long ticksTotal) {
		if (animFrightened.isRunning() && !animFlashing.isRunning()) {
			animFrightened.stop();
			long frameTicks = ticksTotal / (numFlashes * animFlashing.numFrames());
			animFlashing.frameDuration(frameTicks).repetitions(numFlashes).restart();
		}
	}

	private int numberFrame(int number) {
		return switch (number) {
		case 200 -> 0;
		case 400 -> 1;
		case 800 -> 2;
		case 1600 -> 3;
		default -> throw new IllegalArgumentException();
		};
	}

	@Override
	public void render(GraphicsContext g, Rendering2D r2D) {
		var sprite = switch (selected) {
		case EYES -> animEyes.get(ghost.wishDir()).animate();
		case FLASHING -> animFlashing.animate();
		case FRIGHTENED -> animFrightened.animate();
		case KICKING -> animKicking.get(ghost.wishDir()).animate();
		case NUMBER -> animNumber.frame(numberFrame(ghost.bounty));
		};
		r2D.drawEntity(g, ghost, sprite);
	}
}