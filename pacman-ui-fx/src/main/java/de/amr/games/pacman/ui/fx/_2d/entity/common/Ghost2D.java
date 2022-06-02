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

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.lib.ISpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.lib.SpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.lib.SpriteAnimationMap;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * 2D representation of a ghost.
 * 
 * @author Armin Reichert
 */
public class Ghost2D extends GameEntity2D {

	public enum GhostAnimation {
		ALIVE, DEAD, EATEN, FRIGHTENED, RECOVERING;
	};

	public final Ghost ghost;
	public final GhostAnimations animations;

	public Ghost2D(Ghost ghost, GameModel game, GhostAnimations animations) {
		super(game);
		this.ghost = ghost;
		this.animations = animations;
		animations.selectAnimation(GhostAnimation.ALIVE);
	}

	public void updateAnimation(boolean frightened, boolean recovering) {
		GhostAnimation key = switch (ghost.state) {
		case DEAD -> ghost.bounty == 0 ? GhostAnimation.DEAD : GhostAnimation.EATEN;
		case ENTERING_HOUSE -> GhostAnimation.DEAD;
		case FRIGHTENED -> recovering ? GhostAnimation.RECOVERING : GhostAnimation.FRIGHTENED;
		case HUNTING_PAC, LEAVING_HOUSE -> GhostAnimation.ALIVE;
		case LOCKED -> recovering ? GhostAnimation.RECOVERING
				: frightened ? GhostAnimation.FRIGHTENED : GhostAnimation.ALIVE;
		};
		animations.selectAnimation(key);
	}

	public void refresh() {
		visible = ghost.visible;
		animations.refresh();
	}

	@Override
	public void render(GraphicsContext g, Rendering2D r2D) {
		r2D.drawEntity(g, ghost, animations.currentSprite(ghost));
		if (GameUI.debug) {
			renderAnimationState(g);
		}
	}

	private void renderAnimationState(GraphicsContext g) {
		if (!ghost.visible) {
			return;
		}
		g.setFill(Color.WHITE);
		g.setFont(Font.font("Arial Narrow", 10));
		String text = animations.selectedKey().name();
		var animation = unpack(animations.selectedAnimation());
		text += !animation.isRunning() ? " stopped" : "";
		g.fillText(text, ghost.position.x - 10, ghost.position.y - 5);
	}

	@SuppressWarnings("unchecked")
	private SpriteAnimation<Rectangle2D> unpack(ISpriteAnimation animation) {
		if (animation instanceof SpriteAnimation) {
			return (SpriteAnimation<Rectangle2D>) animation;
		} else if (animation instanceof SpriteAnimationMap) {
			var map = (SpriteAnimationMap<Direction, Rectangle2D>) animation;
			return map.get(ghost.wishDir());
		}
		return null;
	}
}