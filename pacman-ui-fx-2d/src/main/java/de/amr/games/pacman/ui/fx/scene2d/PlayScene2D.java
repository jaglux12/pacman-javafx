/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadePalette;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.MsPacManSpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.actors.GhostState.RETURNING_TO_HOUSE;
import static de.amr.games.pacman.ui.fx.PacManGames2dApp.*;

/**
 * 2D play scene.
 *
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

	@Override
	public boolean isCreditVisible() {
		return !context.gameController().hasCredit() || context.gameState() == GameState.GAME_OVER;
	}

	@Override
	public void init() {
		setScoreVisible(true);
	}

	@Override
	public void update() {
		context.gameLevel().ifPresent(this::updateSound);
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(KEYS_ADD_CREDIT)) {
			if (!context.gameController().hasCredit()) {
				context.actionHandler().addCredit();
			}
		} else if (Keyboard.pressed(KEY_CHEAT_EAT_ALL)) {
			context.actionHandler().cheatEatAllPellets();
		} else if (Keyboard.pressed(KEY_CHEAT_ADD_LIVES)) {
			context.actionHandler().cheatAddLives();
		} else if (Keyboard.pressed(KEY_CHEAT_NEXT_LEVEL)) {
			context.actionHandler().cheatEnterNextLevel();
		} else if (Keyboard.pressed(KEY_CHEAT_KILL_GHOSTS)) {
			context.actionHandler().cheatKillAllEatableGhosts();
		}
	}

	@Override
	protected void drawSceneContent() {
		context.gameLevel().ifPresent(level -> {
			if (context.gameVariant() == GameVariant.MS_PACMAN) {
				int mazeNumber = context.game().mazeNumber(level.number());
				drawMsPacManMaze(level, mazeNumber);
			} else {
				drawPacManMaze(level);
			}
			if (context.gameState() == GameState.LEVEL_TEST) {
				drawText(String.format("TEST    L%d", level.number()),
						ArcadePalette.YELLOW, sceneFont(8), t(8.5), t(21));
			} else if (context.gameState() == GameState.GAME_OVER || !context.gameController().hasCredit()) {
				drawText("GAME  OVER", ArcadePalette.RED, sceneFont(8), t(9), t(21));
			} else if (context.gameState() == GameState.READY) {
				drawText("READY!", ArcadePalette.YELLOW, sceneFont(8), t(11), t(21));
			}
			level.bonus().ifPresent(this::drawBonus);
			drawPac(level.pac());
			Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
					.map(level::ghost).forEach(this::drawGhost);
			if (!isCreditVisible()) {
				boolean hideOne = level.pac().isVisible() || context.gameState() == GameState.GHOST_DYING;
				int lives = hideOne ? context.game().lives() - 1 : context.game().lives();
				drawLivesCounter(lives);
			}
			drawLevelCounter();
		});
	}

	// TODO put all images into a single sprite sheet
	private void drawPacManMaze(GameLevel level) {
		var theme = context.theme();
		var world = level.world();
		double x = 0, y = t(3);
		if (world.mazeFlashing().isRunning()) {
			var image = world.mazeFlashing().on()
					? theme.image("pacman.flashingMaze")
					: theme.image("pacman.emptyMaze");
			g.drawImage(image, s(x), s(y), s(image.getWidth()), s(image.getHeight()));
		} else {
			var image = theme.image("pacman.fullMaze");
			g.drawImage(image, s(x), s(y), s(image.getWidth()), s(image.getHeight()));
			world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideTileContent(world, tile));
			if (world.energizerBlinking().off()) {
				world.energizerTiles().forEach(tile -> hideTileContent(world, tile));
			}
		}
	}

	private void drawMsPacManMaze(GameLevel level, int mazeNumber) {
		var theme = context.theme();
		var world = level.world();
		double x = 0, y = t(3);
		var ss = context.<MsPacManSpriteSheet>spriteSheet();
		if (world.mazeFlashing().isRunning()) {
			if (world.mazeFlashing().on()) {
				var source = theme.image("mspacman.flashingMazes");
				var flashingMazeSprite = ss.highlightedMaze(mazeNumber);
				drawSprite(source, flashingMazeSprite, x - 3 /* don't tell your mommy */, y);
			} else {
				drawSprite(ss.source(), ss.emptyMaze(mazeNumber), x, y);
			}
		} else {
			// draw filled maze and hide eaten food (including energizers)
			drawSprite(ss.filledMaze(mazeNumber), x, y);
			world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideTileContent(world, tile));
			// energizer animation
			if (world.energizerBlinking().off()) {
				world.energizerTiles().forEach(tile -> hideTileContent(world, tile));
			}
		}
	}

	private void hideTileContent(World world, Vector2i tile) {
		g.setFill(context.theme().color("canvas.background"));
		double r = world.isEnergizerTile(tile) ? 4.5 : 2;
		double cx = t(tile.x()) + HTS;
		double cy = t(tile.y()) + HTS ;
		g.fillRect(s(cx-r), s(cy-r), s(2*r), s(2*r));
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(GameModel.TILES_X, GameModel.TILES_Y);
		context.gameLevel().ifPresent(level -> level.upwardsBlockedTiles().forEach(tile -> {
			// "No Trespassing" symbol
			g.setFill(Color.RED);
			g.fillOval(s(t(tile.x())), s(t(tile.y() - 1)), s(TS), s(TS));
			g.setFill(Color.WHITE);
			g.fillRect(s(t(tile.x()) + 1), s(t(tile.y()) - HTS - 1), s(TS - 2), s(2));
		}));
		g.setFill(Color.YELLOW);
		g.setFont(Font.font("Sans", 24));
		g.fillText(String.format("%s %d",
				context.gameState(),
				context.gameState().timer().tick()),
				0, 80);
	}

	@Override
	public void onSceneVariantSwitch() {
		context.gameLevel().ifPresent(level -> {
			if (!level.isDemoLevel() && context.gameState() == GameState.HUNTING) {
				context.soundHandler().ensureSirenStarted(level.game().variant(), level.huntingPhase() / 2);
			}
		});
	}

	private void updateSound(GameLevel level) {
		if (level.isDemoLevel()) {
			return;
		}
		if (level.pac().starvingTicks() > 8) { // TODO not sure
			context.clip("audio.pacman_munch").stop();
		}
		if (!level.thisFrame().pacKilled && level.ghosts(RETURNING_TO_HOUSE, ENTERING_HOUSE).anyMatch(Ghost::isVisible)) {
			context.soundHandler().ensureLoopEndless(context.clip("audio.ghost_returning"));
		} else {
			context.clip("audio.ghost_returning").stop();
		}
	}
}