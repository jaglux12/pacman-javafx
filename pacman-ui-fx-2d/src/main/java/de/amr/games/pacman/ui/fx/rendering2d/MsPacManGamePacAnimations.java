/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import de.amr.games.pacman.ui.fx.util.SpriteAnimations;
import javafx.geometry.Rectangle2D;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class MsPacManGamePacAnimations extends SpriteAnimations {

	private final Map<String, SpriteAnimation> animationsByName;
	private final Pac pac;
	private final MsPacManGameSpriteSheet spriteSheet;

	public MsPacManGamePacAnimations(Pac pac, MsPacManGameSpriteSheet spriteSheet) {
		checkNotNull(pac);
		checkNotNull(spriteSheet);
		this.pac = pac;
		this.spriteSheet = spriteSheet;

		var munching = SpriteAnimation.begin()
			.sprites(spriteSheet.msPacManMunchingSprites(Direction.LEFT))
			.loop()
			.end();
		
		var dying = SpriteAnimation.begin()
			.sprites(spriteSheet.msPacManDyingSprites())
			.frameTicks(8)
			.end();
		
		var husbandMunching = SpriteAnimation.begin()
			.sprites(spriteSheet.pacManMunchingSprites(Direction.LEFT))
			.frameTicks(2)
			.loop()
			.end();

		animationsByName = Map.of(
			Pac.ANIM_MUNCHING,         munching,
			Pac.ANIM_DYING,            dying,
			Pac.ANIM_HUSBAND_MUNCHING, husbandMunching
		);
	}

	@Override
	public SpriteAnimation animation(String name) {
		return animationsByName.get(name);
	}

	@Override
	public Rectangle2D currentSprite() {
		var currentAnimation = currentAnimation();
		if (Pac.ANIM_MUNCHING.equals(currentAnimationName)) {
			currentAnimation.setSprites(spriteSheet.msPacManMunchingSprites(pac.moveDir()));
		}
		if (Pac.ANIM_HUSBAND_MUNCHING.equals(currentAnimationName)) {
			currentAnimation.setSprites(spriteSheet.pacManMunchingSprites(pac.moveDir()));
		}
		return currentAnimation != null ? currentAnimation.currentSprite() : null;
	}
}