package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.heaven.God.clock;
import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2f;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.GhostState;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.common.AbstractPacManGameScene;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the PacMan game.
 * 
 * @author Armin Reichert
 */
public class PacManGameIntroScene extends AbstractPacManGameScene<PacManSceneRendering> {

	enum Phase {
		BEGIN, GHOST_GALLERY, CHASING_PAC, CHASING_GHOSTS, READY_TO_PLAY
	}

	private final int titleY = t(6);
	private Ghost[] gallery;
	private int currentGhost;
	private boolean[] characterVisible;
	private boolean[] nickVisible;
	private Pac pac;
	private Ghost[] ghosts;
	private Animation<Boolean> blinking = Animation.pulse().frameDuration(20).restart();

	private Phase phase;
	private long phaseStartTime;

	private boolean phaseAt(long ticks) {
		return clock.ticksTotal - phaseStartTime == ticks;
	}

	private void enterPhase(Phase newPhase) {
		phase = newPhase;
		phaseStartTime = clock.ticksTotal;
		log("Phase %s entered at %d", phase, phaseStartTime);
	}

	public PacManGameIntroScene(PacManGameModel game, double width, double height, double scaling) {
		super(game, null, width, height, scaling);
		setRendering(new PacManSceneRendering(g));
	}

	@Override
	public void start() {
		gallery = new Ghost[] { //
				new Ghost(0, "Blinky", Direction.RIGHT), //
				new Ghost(1, "Pinky", Direction.RIGHT), //
				new Ghost(2, "Inky", Direction.RIGHT), //
				new Ghost(3, "Clyde", Direction.RIGHT), //
		};
		characterVisible = new boolean[4];
		nickVisible = new boolean[4];

		pac = new Pac("Ms. Pac-Man", Direction.LEFT);
		ghosts = new Ghost[] { //
				new Ghost(0, "Blinky", Direction.LEFT), //
				new Ghost(1, "Pinky", Direction.LEFT), //
				new Ghost(2, "Inky", Direction.LEFT), //
				new Ghost(3, "Clyde", Direction.LEFT), //
		};
		enterPhase(Phase.BEGIN);
	}

	@Override
	public void render() {
		fill(Color.BLACK);
		switch (phase) {
		case BEGIN:
			if (phaseAt(clock.sec(2))) {
				presentGhost(0);
				enterPhase(Phase.GHOST_GALLERY);
			}
			break;
		case GHOST_GALLERY:
			if (phaseAt(clock.sec(0.5))) {
				characterVisible[currentGhost] = true;
			}
			if (phaseAt(clock.sec(1))) {
				nickVisible[currentGhost] = true;
			}
			if (phaseAt(clock.sec(2))) {
				if (currentGhost < 3) {
					presentGhost(currentGhost + 1);
					enterPhase(Phase.GHOST_GALLERY);
				} else {
					startGhostsChasingPac();
					enterPhase(Phase.CHASING_PAC);
				}
			}
			break;
		case CHASING_PAC:
			showGuysMoving();
			if (pac.position.x < t(3)) {
				startPacChasingGhosts();
				enterPhase(Phase.CHASING_GHOSTS);
			}
			break;
		case CHASING_GHOSTS:
			showGuysMoving();
			if (pac.position.x > t(28)) {
				enterPhase(Phase.READY_TO_PLAY);
			}
			break;
		case READY_TO_PLAY:
			blinking.animate();
			showPointsAnimation(26);
			showPressKeyToStart(32);
			if (phaseAt(clock.sec(10))) {
				game.attractMode = true;
			}
			break;
		default:
			break;
		}
		rendering.drawScore(game, true);
		drawGallery();
	}

	private void presentGhost(int id) {
		currentGhost = id;
		gallery[id].visible = true;
	}

	private void drawGallery() {
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("CHARACTER", t(6), titleY);
		g.fillText("/", t(16), titleY);
		g.fillText("NICKNAME", t(18), titleY);
		showInGallery(gallery[0], "SHADOW", Color.RED, t(3), titleY + t(2), characterVisible[0], nickVisible[0]);
		showInGallery(gallery[1], "SPEEDY", Color.PINK, t(3), titleY + t(5), characterVisible[1], nickVisible[1]);
		showInGallery(gallery[2], "BASHFUL", Color.CYAN, t(3), titleY + t(8), characterVisible[2], nickVisible[2]);
		showInGallery(gallery[3], "POKEY", Color.ORANGE, t(3), titleY + t(11), characterVisible[3], nickVisible[3]);
	}

	private void showInGallery(Ghost ghost, String character, Color color, int x, int y, boolean showCharacter,
			boolean showName) {
		if (!ghost.visible) {
			return;
		}
		Rectangle2D ghostTile = rendering.ghostKickingToDir(ghost, Direction.RIGHT).frame(0);
		rendering.drawRegion(rendering.toRegion(ghostTile), x, y - 4);
		g.setFill(color);
		g.setFont(rendering.getScoreFont());
		if (showCharacter) {
			g.fillText("-" + character, t(6), y + 8);
		}
		if (showName) {
			g.fillText("\"" + ghost.name + "\"", t(18), y + 8);
		}
	}

	private void showPressKeyToStart(int yTile) {
		if (blinking.frame()) {
			g.setFill(Color.ORANGE);
			g.setFont(rendering.getScoreFont());
			g.fillText("PRESS SPACE KEY TO PLAY", t(2), t(yTile));
		}
	}

	private void showPointsAnimation(int yTile) {
		if (blinking.frame()) {
			g.setFill(Color.PINK);
			g.fillRect(t(8) + 6, t(yTile - 1) + 2, 2, 2);
			g.fillOval(t(8), t(yTile + 1) - 2, 10, 10);
		}
		g.setFill(Color.WHITE);
		g.setFont(rendering.getScoreFont());
		g.fillText("10", t(10), t(yTile));
		g.fillText("50", t(10), t(yTile + 2));
		g.setFont(Font.font(rendering.getScoreFont().getName(), 6));
		g.fillText("PTS", t(13), t(yTile));
		g.fillText("PTS", t(13), t(yTile + 2));
	}

	private void startGhostsChasingPac() {
		pac.position = new V2f(t(28), t(22));
		pac.visible = true;
		pac.speed = 1;
		pac.dir = Direction.LEFT;
		pac.couldMove = true;
		rendering.pacMunching().forEach(Animation::restart);

		for (Ghost ghost : ghosts) {
			ghost.position = pac.position.sum((ghost.id + 1) * 18, 0);
			ghost.visible = true;
			ghost.dir = ghost.wishDir = Direction.LEFT;
			ghost.speed = pac.speed;
			ghost.state = GhostState.HUNTING_PAC;
			rendering.ghostsKicking(Stream.of(ghosts)).forEach(Animation::restart);
		}
	}

	private void showGuysMoving() {
		pac.move();
		rendering.drawPac(pac, game);
		for (Ghost ghost : ghosts) {
			ghost.move();
			rendering.drawGhost(ghost, game);
		}
	}

	private void startPacChasingGhosts() {
		pac.dir = Direction.RIGHT;
		for (Ghost ghost : ghosts) {
			ghost.dir = ghost.wishDir = Direction.RIGHT;
		}
	}
}