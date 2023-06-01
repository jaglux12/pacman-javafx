/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public abstract class PacSpriteAnimationsCommon implements PacAnimations<SpriteAnimation, Rectangle2D> {

	protected final Pac pac;
	protected Spritesheet spritesheet;

	protected final Map<String, SpriteAnimation> animationsByName = new HashMap<>();

	protected String currentAnimationName;
	protected SpriteAnimation currentAnimation;

	protected PacSpriteAnimationsCommon(Pac pac, Spritesheet gss) {
		this.pac = pac;
		this.spritesheet = gss;

		var munching = SpriteAnimation.begin().loop().sprites(munchingSprites(Direction.LEFT)).end();
		var dying = SpriteAnimation.begin().frameTicks(8).sprites(dyingSprites()).end();

		animationsByName.put(PacAnimations.MUNCHING, munching);
		animationsByName.put(PacAnimations.DYING, dying);
	}

	public Spritesheet spritesheet() {
		return spritesheet;
	}

	protected abstract Rectangle2D[] munchingSprites(Direction dir);

	protected abstract Rectangle2D[] dyingSprites();

	@Override
	public void select(String name) {
		if (!name.equals(currentAnimationName)) {
			currentAnimationName = name;
			currentAnimation = byName(name);
			if (currentAnimation != null) {
				currentAnimation.setFrameIndex(0);
			}
		}
	}

	public SpriteAnimation byName(String name) {
		if (animationsByName.containsKey(name)) {
			return animationsByName.get(name);
		}
		throw new IllegalArgumentException("Illegal animation name: " + name);
	}

	@Override
	public SpriteAnimation selectedAnimation() {
		return currentAnimation;
	}

	@Override
	public String selectedAnimationName() {
		return currentAnimationName;
	}

	private void withCurrentAnimationDo(Consumer<SpriteAnimation> operation) {
		if (currentAnimation != null) {
			operation.accept(currentAnimation);
		}
	}

	@Override
	public void startSelected() {
		withCurrentAnimationDo(SpriteAnimation::start);
	}

	@Override
	public void stopSelected() {
		withCurrentAnimationDo(SpriteAnimation::stop);
	}

	@Override
	public void resetSelected() {
		withCurrentAnimationDo(SpriteAnimation::reset);
	}

	@Override
	public Rectangle2D currentSprite() {
		if (currentAnimationName == null) {
			return null;
		}
		if (MUNCHING.equals(currentAnimationName)) {
			currentAnimation.setSprites(munchingSprites(pac.moveDir()));
		}
		return currentAnimation.currentSprite();
	}
}