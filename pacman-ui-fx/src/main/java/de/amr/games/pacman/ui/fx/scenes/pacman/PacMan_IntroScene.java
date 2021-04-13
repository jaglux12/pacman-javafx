package de.amr.games.pacman.ui.fx.scenes.pacman;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;
import static de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.TOP_Y;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.entities._2d.GameScore2D;
import de.amr.games.pacman.ui.fx.entities._2d.Ghost2D;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Impl;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller;
import de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.GhostPortrait;
import de.amr.games.pacman.ui.pacman.PacMan_IntroScene_Controller.Phase;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card
 * and hunts the ghost himself.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntroScene extends AbstractGameScene2D<Rendering2D_PacMan> {

	private PacMan_IntroScene_Controller sceneController;

	private GameScore2D<Rendering2D_PacMan> score2D;
	private GameScore2D<Rendering2D_PacMan> hiscore2D;
	private Player2D<Rendering2D_PacMan> pacMan2D;
	private List<Ghost2D<Rendering2D_PacMan>> ghosts2D;
	private List<Ghost2D<Rendering2D_PacMan>> ghostsInGallery2D;

	public PacMan_IntroScene() {
		super(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT, Rendering2D_Impl.RENDERING_PACMAN, PacManScenes.SOUNDS);
	}

	@Override
	public void start() {
		super.start();
		sceneController = new PacMan_IntroScene_Controller(gameController);
		sceneController.init();

		score2D = new GameScore2D<>(rendering);
		score2D.setTitle("SCORE");
		score2D.setLeftUpperCorner(new V2i(1, 1));
		score2D.setLevelSupplier(() -> game().currentLevelNumber);
		score2D.setPointsSupplier(() -> game().score);

		hiscore2D = new GameScore2D<>(rendering);
		hiscore2D.setTitle("HI SCORE");
		hiscore2D.setLeftUpperCorner(new V2i(16, 1));
		hiscore2D.setLevelSupplier(() -> game().highscoreLevel);
		hiscore2D.setPointsSupplier(() -> game().highscorePoints);

		pacMan2D = new Player2D<>(sceneController.pac, rendering);
		pacMan2D.getMunchingAnimations().values().forEach(TimedSequence::restart);

		ghosts2D = Stream.of(sceneController.ghosts).map(ghost -> {
			Ghost2D<Rendering2D_PacMan> ghost2D = new Ghost2D<>(ghost, rendering);
			ghost2D.getKickingAnimations().values().forEach(TimedSequence::restart);
			ghost2D.getFrightenedAnimation().restart();
			ghost2D.getFlashingAnimation().restart();
			return ghost2D;
		}).collect(Collectors.toList());

		ghostsInGallery2D = new ArrayList<>();
		for (int i = 0; i < 4; ++i) {
			Ghost2D<Rendering2D_PacMan> ghost2D = new Ghost2D<>(sceneController.gallery[i].ghost, rendering);
			ghostsInGallery2D.add(ghost2D);
		}
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void render() {
		score2D.setShowPoints(false);
		score2D.render(gc);
		hiscore2D.render(gc);
		drawGallery();
		if (sceneController.phase == Phase.CHASING_PAC) {
			if (sceneController.blinking.animate()) {
				gc.setFill(Color.PINK);
				gc.fillOval(t(2), sceneController.pac.position.y, TS, TS);
			}
		}
		ghosts2D.forEach(ghost2D -> ghost2D.render(gc));
		pacMan2D.render(gc);
		if (sceneController.phase.ordinal() >= Phase.CHASING_GHOSTS.ordinal()) {
			drawPointsAnimation(11, 26);
		}
		if (sceneController.phase == Phase.READY_TO_PLAY) {
			drawPressKeyToStart(32);
		}
	}

	private void drawGallery() {
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("CHARACTER", t(6), TOP_Y);
		gc.fillText("/", t(16), TOP_Y);
		gc.fillText("NICKNAME", t(18), TOP_Y);
		for (int i = 0; i < 4; ++i) {
			GhostPortrait portrait = sceneController.gallery[i];
			if (portrait.ghost.visible) {
				int y = TOP_Y + t(2 + 3 * i);
				ghostsInGallery2D.get(i).render(gc);
				gc.setFill(getGhostColor(i));
				gc.setFont(rendering.getScoreFont());
				if (portrait.characterVisible) {
					gc.fillText("-" + portrait.character, t(6), y + 8);
				}
				if (portrait.nicknameVisible) {
					gc.fillText("\"" + portrait.ghost.name + "\"", t(18), y + 8);
				}
			}
		}
	}

	private Color getGhostColor(int i) {
		return i == 0 ? Color.RED : i == 1 ? Color.PINK : i == 2 ? Color.CYAN : Color.ORANGE;
	}

	private void drawPressKeyToStart(int yTile) {
		if (sceneController.blinking.frame()) {
			String text = "PRESS SPACE TO PLAY";
			gc.setFill(Color.ORANGE);
			gc.setFont(rendering.getScoreFont());
			gc.fillText(text, t(14 - text.length() / 2), t(yTile));
		}
	}

	private void drawPointsAnimation(int tileX, int tileY) {
		if (sceneController.blinking.frame()) {
			gc.setFill(Color.PINK);
			gc.fillRect(t(tileX) + 6, t(tileY - 1) + 2, 2, 2);
			gc.fillOval(t(tileX), t(tileY + 1) - 2, 10, 10);
		}
		gc.setFill(Color.WHITE);
		gc.setFont(rendering.getScoreFont());
		gc.fillText("10", t(tileX + 2), t(tileY));
		gc.fillText("50", t(tileX + 2), t(tileY + 2));
		gc.setFont(Font.font(rendering.getScoreFont().getName(), 6));
		gc.fillText("PTS", t(tileX + 5), t(tileY));
		gc.fillText("PTS", t(tileX + 5), t(tileY + 2));
	}
}