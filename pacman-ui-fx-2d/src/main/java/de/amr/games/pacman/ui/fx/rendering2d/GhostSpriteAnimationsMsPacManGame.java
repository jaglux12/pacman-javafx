/**
 * 
 */
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostSpriteAnimationsMsPacManGame extends GhostSpriteAnimationsCommon {

	public GhostSpriteAnimationsMsPacManGame(Ghost ghost, SpritesheetMsPacManGame spritesheet) {
		super(ghost, spritesheet);
	}

	@Override
	public SpritesheetMsPacManGame spritesheet() {
		return (SpritesheetMsPacManGame) super.spritesheet();
	}

	@Override
	protected Rectangle2D[] eyesSprites(Direction dir) {
		return spritesheet().ghostEyesSprites(dir);
	}

	@Override
	protected Rectangle2D[] flashingSprites() {
		return spritesheet().ghostFlashingSprites();
	}

	@Override
	protected Rectangle2D[] frightenedSprites() {
		return spritesheet().ghostFrightenedSprites();
	}

	@Override
	protected Rectangle2D[] normalSprites(byte id, Direction dir) {
		return spritesheet().ghostNormalSprites(id, dir);
	}

	@Override
	protected Rectangle2D[] numberSprites() {
		return spritesheet().ghostNumberSprites();
	}
}