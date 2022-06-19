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
package de.amr.games.pacman.ui.fx._2d.rendering.common;

import java.util.HashMap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.SpriteAnimationMap;
import de.amr.games.pacman.lib.animation.SpriteAnimations;
import de.amr.games.pacman.model.common.actors.Ghost;
import javafx.geometry.Rectangle2D;

/**
 * @author Armin Reichert
 */
public class GhostAnimations extends SpriteAnimations<Ghost> {

	public static final String BLUE = "blue";
	public static final String COLOR = "color";
	public static final String EYES = "eyes";
	public static final String FLASHING = "flashing";
	public static final String VALUE = "value";

	public GhostAnimations(int ghostID, Rendering2D r2D) {
		animationsByName = new HashMap<>();
		animationsByName.put(BLUE, r2D.createGhostBlueAnimation());
		animationsByName.put(EYES, r2D.createGhostEyesAnimationMap());
		animationsByName.put(FLASHING, r2D.createGhostFlashingAnimation());
		animationsByName.put(COLOR, r2D.createGhostColorAnimationMap(ghostID));
		animationsByName.put(VALUE, r2D.createGhostValueList());
		select(COLOR);
	}

	@Override
	public Rectangle2D current(Ghost ghost) {
		return (Rectangle2D) switch (selected) {
		case EYES -> toMap(EYES).get(ghost.wishDir()).animate();
		case COLOR -> toMap(COLOR).get(ghost.wishDir()).animate();
		default -> selectedAnimation().animate();
		};
	}

	private SpriteAnimationMap<Direction, Rectangle2D> toMap(String name) {
		return super.<Direction, Rectangle2D>castToMap(name);
	}
}