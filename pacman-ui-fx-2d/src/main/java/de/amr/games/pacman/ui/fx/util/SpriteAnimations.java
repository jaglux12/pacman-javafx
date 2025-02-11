/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import de.amr.games.pacman.model.actors.Animations;
import javafx.geometry.Rectangle2D;
import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public abstract class SpriteAnimations implements Animations {

	protected String currentAnimationName;

	public String currentAnimationName() {
		return currentAnimationName;
	}

	@Override
	public SpriteAnimation currentAnimation() {
		return animation(currentAnimationName);
	}

	public abstract Rectangle2D currentSprite();

	public abstract SpriteAnimation animation(String name);

	@Override
	public void select(String name, int index) {
		if (!name.equals(currentAnimationName)) {
			currentAnimationName = name;
			if (currentAnimation() != null) {
				currentAnimation().setFrameIndex(0);
			} else {
				Logger.warn("No animation with name {} exists", name);
			}
		}
	}

	@Override
	public void startSelected() {
		if (currentAnimation() != null) {
			currentAnimation().start();
		}
	}

	@Override
	public void stopSelected() {
		if (currentAnimation() != null) {
			currentAnimation().stop();
		}
	}

	@Override
	public void resetSelected() {
		if (currentAnimation() != null) {
			currentAnimation().reset();
		}
	}
}