/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import de.amr.games.pacman.ui.fx.util.SpriteAnimations;
import javafx.geometry.Rectangle2D;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacManGameGhostAnimations extends SpriteAnimations {

	private final Map<String, SpriteAnimation> animationsByName;
	private final Ghost ghost;
	private final PacManGameSpriteSheet spriteSheet;

	public PacManGameGhostAnimations(Ghost ghost, PacManGameSpriteSheet spriteSheet) {
		checkNotNull(ghost);
		checkNotNull(spriteSheet);
		this.ghost = ghost;
		this.spriteSheet = spriteSheet;

		var normal = SpriteAnimation.begin()
			.sprites(spriteSheet.ghostNormalSprites(ghost.id(), Direction.LEFT))
			.frameTicks(8)
			.loop()
			.end();

		var frightened = SpriteAnimation.begin()
			.sprites(spriteSheet.ghostFrightenedSprites())
			.frameTicks(8)
			.loop()
			.end();
		
		var flashing = SpriteAnimation.begin()
			.sprites(spriteSheet.ghostFlashingSprites())
			.frameTicks(6)
			.loop()
			.end();
		
		var eyesAnimation = SpriteAnimation.begin()
			.sprites(spriteSheet.ghostEyesSprites(Direction.LEFT))
			.end();
		
		var numberAnimation = SpriteAnimation.begin()
			.sprites(spriteSheet.ghostNumberSprites())
			.end();
		
		var damaged = SpriteAnimation.begin()
			.sprites(spriteSheet.blinkyDamagedSprites())
			.end();
		
		var stretched = SpriteAnimation.begin()
			.sprites(spriteSheet.blinkyStretchedSprites())
			.end();
		
		var patched = SpriteAnimation.begin()
			.sprites(spriteSheet.blinkyPatchedSprites())
			.frameTicks(4)
			.loop()
			.end();
		
		var naked = SpriteAnimation.begin()
			.sprites(spriteSheet.blinkyNakedSprites())
			.frameTicks(4)
			.loop()
			.end();

		animationsByName = Map.of(
			Ghost.ANIM_GHOST_NORMAL,     normal,
			Ghost.ANIM_GHOST_FRIGHTENED, frightened,
			Ghost.ANIM_GHOST_FLASHING,   flashing,
			Ghost.ANIM_GHOST_EYES,       eyesAnimation,
			Ghost.ANIM_GHOST_NUMBER,     numberAnimation,
			Ghost.ANIM_BLINKY_DAMAGED,   damaged,
			Ghost.ANIM_BLINKY_STRETCHED, stretched,
			Ghost.ANIM_BLINKY_PATCHED,   patched,
			Ghost.ANIM_BLINKY_NAKED,     naked);

		// TODO check this
		eyesAnimation.start();
		frightened.start();
		flashing.start();
	}

	@Override
	public SpriteAnimation animation(String name) {
		return animationsByName.get(name);
	}

	@Override
	public void select(String name, Object... args) {
		super.select(name, args);
		if (Ghost.ANIM_GHOST_NUMBER.equals(name)) {
			animation(Ghost.ANIM_GHOST_NUMBER).setFrameIndex((Byte) args[0]);
		}
	}

	@Override
	public Rectangle2D currentSprite() {
		var currentAnimation = currentAnimation();
		if (Ghost.ANIM_GHOST_NORMAL.equals(currentAnimationName)) {
			currentAnimation.setSprites(spriteSheet.ghostNormalSprites(ghost.id(), ghost.wishDir()));
		} else if (Ghost.ANIM_GHOST_EYES.equals(currentAnimationName)) {
			currentAnimation.setSprites(spriteSheet.ghostEyesSprites(ghost.wishDir()));
		}
		return currentAnimation != null ? currentAnimation.currentSprite() : null;
	}
}