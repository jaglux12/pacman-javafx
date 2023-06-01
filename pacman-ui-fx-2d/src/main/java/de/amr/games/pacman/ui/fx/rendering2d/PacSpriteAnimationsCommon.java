/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

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
public abstract class PacSpriteAnimationsCommon implements PacAnimations<SpriteAnimation> {

	protected final Pac pac;
	protected Spritesheet spritesheet;

	protected SpriteAnimation munchingAnimation;
	protected SpriteAnimation dyingAnimation;

	protected String currentAnimationName;
	protected SpriteAnimation currentAnimation;

	protected PacSpriteAnimationsCommon(Pac pac, Spritesheet gss) {
		this.pac = pac;
		this.spritesheet = gss;
		munchingAnimation = new SpriteAnimation.Builder() //
				.loop() //
				.sprites(munchingSprites(Direction.LEFT)) //
				.build();
		dyingAnimation = new SpriteAnimation.Builder() //
				.frameDurationTicks(8) //
				.sprites(dyingSprites()) //
				.build();
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
			currentAnimation = animation(name, pac.moveDir());
			if (currentAnimation != null) {
				currentAnimation.setFrameIndex(0);
			}
		}
	}

	@Override
	public SpriteAnimation selectedAnimation() {
		return currentAnimation;
	}

	@Override
	public String selectedAnimationName() {
		return currentAnimationName;
	}

	protected SpriteAnimation animation(String name, Direction dir) {
		if (PAC_MUNCHING.equals(name)) {
			return munchingAnimation;
		}
		if (PAC_DYING.equals(name)) {
			return dyingAnimation;
		}
		throw new IllegalArgumentException("Illegal animation (name, dir) value: " + name + "," + dir);
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
		if (!pac.isVisible() || currentAnimationName == null) {
			return null;
		}
		if (currentAnimation == munchingAnimation) {
			munchingAnimation.setSprites(munchingSprites(pac.moveDir()));
		}
		return currentAnimation.currentSprite();
	}
}