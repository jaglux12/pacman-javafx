/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameGhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGamePacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.lib.Globals.v2i;

/**
 * @author Armin Reichert
 */
public class PacManCutscene3 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;

	@Override
	public boolean isCreditVisible() {
		return !context.gameController().hasCredit();
	}

	@Override
	public void init() {
		frame = -1;
		initialDelay = 120;
		setScoreVisible(true);
		var ss = context.<PacManGameSpriteSheet>spriteSheet();
		pac = new Pac("Pac-Man");
		pac.setAnimations(new PacManGamePacAnimations(pac, ss));
		pac.selectAnimation(Pac.ANIM_MUNCHING);
		pac.startAnimation();
		pac.centerOverTile(v2i(29, 20));
		pac.setMoveDir(Direction.LEFT);
		pac.setPixelSpeed(1.25f);
		pac.show();
		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.setAnimations(new PacManGameGhostAnimations(blinky, ss));
		blinky.selectAnimation(Ghost.ANIM_BLINKY_PATCHED);
		blinky.startAnimation();
		blinky.centerOverTile(v2i(35, 20));
		blinky.setMoveAndWishDir(Direction.LEFT);
		blinky.setPixelSpeed(1.25f);
		blinky.show();
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				context.gameController().publishGameEvent(GameEventType.INTERMISSION_STARTED);
			}
			return;
		}
		if (context.gameState().timer().hasExpired()) {
			return;
		}
		switch (++frame) {
			case 400 -> {
				blinky.centerOverTile(v2i(-1, 20));
				blinky.setMoveAndWishDir(Direction.RIGHT);
				blinky.selectAnimation(Ghost.ANIM_BLINKY_NAKED);
				blinky.startAnimation();
			}
			case 700 -> context.gameState().timer().expire();
			default -> {}
		}
		pac.move();
		blinky.move();
	}

	@Override
	public void drawSceneContent() {
		drawPac(pac);
		drawGhost(blinky);
		drawLevelCounter();
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(ArcadeWorld.TILES_X, ArcadeWorld.TILES_Y);
		var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
		drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
	}
}