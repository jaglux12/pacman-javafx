package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.model.guys.GhostState.DEAD;
import static de.amr.games.pacman.model.guys.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.guys.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.guys.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.Creature;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.ui.fx.common.Helper;
import de.amr.games.pacman.ui.fx.common.SpritesheetBasedRendering;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Rendering for the scenes of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacMan_Rendering extends SpritesheetBasedRendering {

	private final Image mazeFull = new Image("/pacman/graphics/maze_full.png", false);
	private final Image mazeEmpty = new Image("/pacman/graphics/maze_empty.png", false);

	private final Animation<Rectangle2D> pacCollapsingAnim;
	private final Animation<Image> mazeFlashingAnim;

	private void drawCreatureRegion(GraphicsContext g, Creature guy, Rectangle2D region) {
		if (guy.visible && region != null) {
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(),
					guy.position.x - 4, guy.position.y - 4, region.getWidth(), region.getHeight());
		}
	}

	public PacMan_Rendering() {
		super(new Image("/pacman/graphics/sprites.png", false));

		symbolRegions = Arrays.asList(tileRegion(2, 3), tileRegion(3, 3), tileRegion(4, 3), tileRegion(5, 3),
				tileRegion(6, 3), tileRegion(7, 3), tileRegion(8, 3), tileRegion(9, 3));

		//@formatter:off
		bonusValueRegions = new HashMap<>();
		bonusValueRegions.put(100,  tileRegion(0, 9, 1, 1));
		bonusValueRegions.put(300,  tileRegion(1, 9, 1, 1));
		bonusValueRegions.put(500,  tileRegion(2, 9, 1, 1));
		bonusValueRegions.put(700,  tileRegion(3, 9, 1, 1));
		bonusValueRegions.put(1000, tileRegion(4, 9, 2, 1)); // left-aligned 
		bonusValueRegions.put(2000, tileRegion(3, 10, 3, 1));
		bonusValueRegions.put(3000, tileRegion(3, 11, 3, 1));
		bonusValueRegions.put(5000, tileRegion(3, 12, 3, 1));

		bountyValueRegions = new HashMap<>();
		bountyValueRegions.put(200,  tileRegion(0, 8, 1, 1));
		bountyValueRegions.put(400,  tileRegion(1, 8, 1, 1));
		bountyValueRegions.put(800,  tileRegion(2, 8, 1, 1));
		bountyValueRegions.put(1600, tileRegion(3, 8, 1, 1));
		//@formatter:on

		// Animations

		Image mazeEmptyBright = Helper.exchangeColors(mazeEmpty, Map.of(getMazeWallBorderColor(0), Color.WHITE));
		mazeFlashingAnim = Animation.of(mazeEmptyBright, mazeEmpty).frameDuration(15);

		pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<Rectangle2D> animation = Animation.of(tileRegion(2, 0), tileRegion(1, index(dir)),
					tileRegion(0, index(dir)), tileRegion(1, index(dir)));
			animation.frameDuration(2).endless().run();
			pacManMunchingAnim.put(dir, animation);
		}

		pacCollapsingAnim = Animation.of(tileRegion(3, 0), tileRegion(4, 0), tileRegion(5, 0), tileRegion(6, 0),
				tileRegion(7, 0), tileRegion(8, 0), tileRegion(9, 0), tileRegion(10, 0), tileRegion(11, 0), tileRegion(12, 0),
				tileRegion(13, 0));
		pacCollapsingAnim.frameDuration(8);

		ghostsKickingAnim = new ArrayList<>(4);
		for (int id = 0; id < 4; ++id) {
			EnumMap<Direction, Animation<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				Animation<Rectangle2D> animation = Animation.of(tileRegion(2 * index(dir), 4 + id),
						tileRegion(2 * index(dir) + 1, 4 + id));
				animation.frameDuration(10).endless();
				walkingTo.put(dir, animation);
			}
			ghostsKickingAnim.add(walkingTo);
		}

		ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnim.put(dir, Animation.of(tileRegion(8 + index(dir), 5)));
		}

		ghostBlueAnim = Animation.of(tileRegion(8, 4), tileRegion(9, 4));
		ghostBlueAnim.frameDuration(20).endless();

		ghostFlashingAnim = Animation.of(tileRegion(8, 4), tileRegion(9, 4), tileRegion(10, 4), tileRegion(11, 4));
		ghostFlashingAnim.frameDuration(5).endless();
	}

	@Override
	public Color getMazeWallBorderColor(int mazeIndex) {
		return Color.rgb(33, 33, 255);
	}

	@Override
	public Color getMazeWallColor(int mazeIndex) {
		return Color.BLACK;
	}

	@Override
	public void drawTileCovered(GraphicsContext g, V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
	}

	@Override
	public void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y);
		} else {
			g.drawImage(mazeFull, x, y);
		}
	}

	@Override
	public void drawLevelCounter(GraphicsContext g, GameModel game, int rightX, int y) {
		int x = rightX;
		int firstLevel = Math.max(1, game.currentLevelNumber - 6);
		for (int level = firstLevel; level <= game.currentLevelNumber; ++level) {
			Rectangle2D region = symbolRegions.get(game.levelSymbols.get(level - 1));
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
					region.getWidth(), region.getHeight());
			x -= t(2);
		}
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, GameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		Rectangle2D region = tileRegion(8, 1);
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x + t(2 * i),
					y, region.getWidth(), region.getHeight());
		}
	}

	@Override
	public void drawFoodTiles(GraphicsContext g, Stream<V2i> tiles, Predicate<V2i> eaten) {
		tiles.filter(eaten).forEach(tile -> drawTileCovered(g, tile));
	}

	@Override
	public void drawEnergizerTiles(GraphicsContext g, Stream<V2i> energizerTiles) {
		if (energizerBlinking.animate()) {
			energizerTiles.forEach(tile -> drawTileCovered(g, tile));
		}
	}

	@Override
	public void drawPac(GraphicsContext g, Pac pac) {
		drawCreatureRegion(g, pac, pacSpriteRegion(pac));
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost, boolean frightened) {
		drawCreatureRegion(g, ghost, ghostSpriteRegion(ghost, frightened));
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		drawCreatureRegion(g, bonus, bonusSpriteRegion(bonus));
	}

	@Override
	public Rectangle2D bonusSpriteRegion(Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			return symbolRegions.get(bonus.symbol);
		}
		if (bonus.eatenTicksLeft > 0) {
			return bonusValueRegions.get(bonus.points);
		}
		return null;
	}

	@Override
	public Rectangle2D pacSpriteRegion(Pac pac) {
		if (pac.dead) {
			return pacDying().hasStarted() ? pacDying().animate() : pacMunchingToDir(pac, pac.dir).frame();
		}
		if (pac.speed == 0) {
			return pacMunchingToDir(pac, pac.dir).frame(0);
		}
		if (!pac.couldMove) {
			return pacMunchingToDir(pac, pac.dir).frame(1);
		}
		return pacMunchingToDir(pac, pac.dir).animate();
	}

	@Override
	public Rectangle2D ghostSpriteRegion(Ghost ghost, boolean frightened) {
		if (ghost.bounty > 0) {
			return bountyValueRegions.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHomeToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && frightened) {
			return ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		return ghostKickingToDir(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
	}

	@Override
	public Animation<Rectangle2D> pacMunchingToDir(Pac pac, Direction dir) {
		return pacManMunchingAnim.get(ensureDirection(dir));
	}

	@Override
	public Animation<Rectangle2D> pacDying() {
		return pacCollapsingAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostKickingToDir(Ghost ghost, Direction dir) {
		return ghostsKickingAnim.get(ghost.id).get(ensureDirection(dir));
	}

	@Override
	public Animation<Rectangle2D> ghostFrightenedToDir(Ghost ghost, Direction dir) {
		return ghostBlueAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostFlashing() {
		return ghostFlashingAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostReturningHomeToDir(Ghost ghost, Direction dir) {
		return ghostEyesAnim.get(ensureDirection(dir));
	}

	@Override
	public Animation<Image> mazeFlashing(int mazeNumber) {
		return mazeFlashingAnim;
	}

	@Override
	public Stream<Animation<?>> mazeFlashings() {
		return Stream.of(mazeFlashingAnim);
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return energizerBlinking;
	}
}