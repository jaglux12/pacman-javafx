/*
MIT License

Copyright (c) 2021 Armin Reichert

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
package de.amr.games.pacman.ui.fx._3d.scene;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.ui.fx.shell.FlashMessageView.showFlashMessage;
import static de.amr.games.pacman.ui.fx.util.U.afterSeconds;
import static de.amr.games.pacman.ui.fx.util.U.pause;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.LevelCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.LivesCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.entity.Score3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.CoordinateSystem;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D extends DefaultGameEventHandler implements GameScene {

	private final GameController gameController;
	private final SubScene fxSubScene;
	private final PacManModel3D model3D;
	private final Image floorImage = new Image(getClass().getResource("/common/escher-texture.jpg").toString());
	private final CoordinateSystem coordSystem;

	private GameModel game;
	private SoundManager sounds;
	private Rendering2D r2D;
	private CameraController camController;
	private Pac3D player3D;
	private Maze3D maze3D;
	private Ghost3D[] ghosts3D;
	private Bonus3D bonus3D;
	private Score3D score3D;
	private LevelCounter3D levelCounter3D;
	private LivesCounter3D livesCounter3D;

	public PlayScene3D(Scene parent, GameController gameController, PacManModel3D model3D) {
		this.gameController = gameController;
		this.model3D = model3D;
		fxSubScene = new SubScene(new Group(), parent.getWidth(), parent.getHeight(), true, SceneAntialiasing.BALANCED);
		fxSubScene.widthProperty().bind(parent.widthProperty());
		fxSubScene.heightProperty().bind(parent.heightProperty());
		fxSubScene.setCamera(new PerspectiveCamera(true));
		coordSystem = new CoordinateSystem(Math.max(fxSubScene.getWidth(), fxSubScene.getHeight()));
		coordSystem.visibleProperty().bind(Env.$axesVisible);
		log("Subscene created. Game scene='%s', width=%.0f, height=%.0f", getClass().getName(), fxSubScene.getWidth(),
				fxSubScene.getHeight());
	}

	@Override
	public void setContext(GameModel game, Rendering2D r2d, SoundManager sounds) {
		this.game = game;
		this.r2D = r2d;
		this.sounds = sounds;
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	@Override
	public SoundManager getSounds() {
		return sounds;
	}

	public CameraController getCamController() {
		return camController;
	}

	@Override
	public void init() {
		final int width = game.world.numCols() * TS;
		final int height = game.world.numRows() * TS;

		maze3D = new Maze3D(width, height);
		maze3D.createWallsAndDoors(game.world, r2D.getMazeSideColor(game.mazeNumber), r2D.getMazeTopColor(game.mazeNumber));
		maze3D.createFood(game.world, r2D.getFoodColor(game.mazeNumber));
		onUseFloorTextureChange(null);

		player3D = new Pac3D(game.player, model3D);
		ghosts3D = game.ghosts().map(ghost -> new Ghost3D(ghost, model3D, r2D)).toArray(Ghost3D[]::new);
		bonus3D = new Bonus3D(r2D);

		score3D = new Score3D();
		score3D.setFont(r2D.getArcadeFont());
		score3D.setComputeScoreText(!gameController.attractMode);
		if (gameController.attractMode) {
			score3D.txtScore.setFill(Color.RED);
			score3D.txtScore.setText("GAME OVER!");
		}

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.getTransforms().add(new Translate(TS, TS, -HTS));
		livesCounter3D.setVisible(!gameController.attractMode);

		levelCounter3D = new LevelCounter3D(r2D, width - TS, TS);
		levelCounter3D.update(game);

		var world3D = new Group(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		world3D.getChildren().addAll(ghosts3D);
		world3D.getTransforms().add(new Translate(-width / 2, -height / 2)); // center at origin

		fxSubScene.setRoot(new Group(new AmbientLight(Color.GHOSTWHITE), world3D, coordSystem));

		onPerspectiveChange(null);

		sounds.setMuted(gameController.attractMode);

		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		maze3D.$resolution.bind(Env.$mazeResolution);
		maze3D.$resolution.addListener(this::onMazeResolutionChange);

		Env.$perspective.addListener(this::onPerspectiveChange);
		Env.$useMazeFloorTexture.addListener(this::onUseFloorTextureChange);
	}

	@Override
	public void end() {
		sounds.setMuted(false);

		maze3D.$wallHeight.unbind();
		maze3D.$resolution.unbind();
		maze3D.$resolution.removeListener(this::onMazeResolutionChange);

		Env.$perspective.removeListener(this::onPerspectiveChange);
		Env.$useMazeFloorTexture.removeListener(this::onUseFloorTextureChange);
	}

	@Override
	public void update() {
		maze3D.update(game);
		player3D.update();
		Stream.of(ghosts3D).forEach(Ghost3D::update);
		bonus3D.update(game.bonus);
		score3D.update(game.score, game.levelNumber, game.highscorePoints, game.highscoreLevel);
		livesCounter3D.update(game.player.lives);
		camController.update();

		// keep in sync with 2D scene in case user toggles between 2D and 3D
		maze3D.pellets().forEach(pellet -> pellet.setVisible(!game.world.isFoodEaten(pellet.tile)));
		if (gameController.state == GameState.HUNTING || gameController.state == GameState.GHOST_DYING) {
			maze3D.energizerAnimations().forEach(Animation::play);
		}
		if (sounds.getClip(GameSounds.PACMAN_MUNCH).isPlaying() && game.player.starvingTicks > 10) {
			sounds.stop(GameSounds.PACMAN_MUNCH);
		}
	}

	private void onMazeResolutionChange(ObservableValue<? extends Number> property, Number oldValue, Number newValue) {
		if (!oldValue.equals(newValue)) {
			maze3D.createWallsAndDoors(game.world, r2D.getMazeSideColor(game.mazeNumber),
					r2D.getMazeTopColor(game.mazeNumber));
		}
	}

	private void onPerspectiveChange(Observable unused) {
		Camera cam = fxSubScene.getCamera();
		camController = switch (Env.$perspective.get()) {
		case CAM_DRONE -> new Cam_Drone(cam, player3D);
		case CAM_FOLLOWING_PLAYER -> new Cam_FollowingPlayer(cam, player3D);
		case CAM_NEAR_PLAYER -> new Cam_NearPlayer(cam, player3D);
		case CAM_TOTAL -> new Cam_Total(cam);
		};
		camController.reset();
		fxSubScene.setOnKeyPressed(camController);
		fxSubScene.requestFocus();
		if (score3D != null) {
			// keep the score in plain sight
			score3D.rotationAxisProperty().bind(camController.cam().rotationAxisProperty());
			score3D.rotateProperty().bind(camController.cam().rotateProperty());
		}
	}

	private void onUseFloorTextureChange(Observable unused) {
		if (Env.$useMazeFloorTexture.get()) {
			maze3D.setFloorTexture(floorImage);
			maze3D.setFloorColor(Color.DARKBLUE);
		} else {
			maze3D.setFloorTexture(null);
			maze3D.setFloorColor(Color.rgb(30, 30, 30));
		}
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		if (e.scatterPhase > 0) {
			sounds.stop(GameSounds.SIRENS.get(e.scatterPhase - 1));
		}
		GameSounds siren = GameSounds.SIRENS.get(e.scatterPhase);
		if (!sounds.getClip(siren).isPlaying())
			sounds.loop(siren, Animation.INDEFINITE);
	}

	@Override
	public void onPlayerGainsPower(GameEvent e) {
		sounds.loop(GameSounds.PACMAN_POWER, Animation.INDEFINITE);
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.creature.is(GhostState.FRIGHTENED) || ghost3D.creature.is(GhostState.LOCKED))
				.forEach(Ghost3D::setFrightenedSkinColor);
	}

	@Override
	public void onPlayerLosingPower(GameEvent e) {
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.creature.is(GhostState.FRIGHTENED)) //
				.forEach(Ghost3D::playFlashingAnimation);
	}

	@Override
	public void onPlayerLostPower(GameEvent e) {
		sounds.stop(GameSounds.PACMAN_POWER);
		Stream.of(ghosts3D).forEach(Ghost3D::setNormalSkinColor);
	}

	@Override
	public void onPlayerFoundFood(GameEvent e) {
		// when cheat "eat all pellets" is used, no tile is present
		e.tile.ifPresent(tile -> {
			maze3D.pelletAt(tile).ifPresent(maze3D::hidePellet);
			AudioClip munching = sounds.getClip(GameSounds.PACMAN_MUNCH);
			if (!munching.isPlaying()) {
				sounds.loop(GameSounds.PACMAN_MUNCH, Animation.INDEFINITE);
			}
		});
	}

	@Override
	public void onBonusActivated(GameEvent e) {
		bonus3D.showSymbol(game.bonus.symbol);
	}

	@Override
	public void onBonusEaten(GameEvent e) {
		bonus3D.showPoints(game.bonus.points);
		sounds.play(GameSounds.BONUS_EATEN);
	}

	@Override
	public void onBonusExpired(GameEvent e) {
		bonus3D.setVisible(false);
	}

	@Override
	public void onExtraLife(GameEvent e) {
		showFlashMessage(1.5, Env.message("extra_life"));
		sounds.play(GameSounds.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturnsHome(GameEvent e) {
		sounds.play(GameSounds.GHOST_RETURNING);
	}

	@Override
	public void onGhostEntersHouse(GameEvent e) {
		if (game.ghosts(GhostState.DEAD).count() == 0) {
			sounds.stop(GameSounds.GHOST_RETURNING);
		}
	}

	@Override
	public void onGhostRevived(GameEvent e) {
		Ghost ghost = e.ghost.get();
		ghosts3D[ghost.id].playRevivalAnimation();
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {

		switch (e.newGameState) {
		case READY -> {
			maze3D.reset();
			maze3D.energizerAnimations().forEach(Animation::stop);
			player3D.reset();
			Stream.of(ghosts3D).forEach(Ghost3D::reset);
			sounds.stopAll();
			sounds.setMuted(gameController.attractMode);
			if (!gameController.gameRunning) {
				sounds.play(GameSounds.GAME_READY);
			}
		}
		case HUNTING -> {
			maze3D.energizerAnimations().forEach(Animation::play);
		}
		case PACMAN_DYING -> {
			Stream.of(ghosts3D).forEach(Ghost3D::setNormalSkinColor);
			sounds.stopAll();
			Ghost killer = Stream.of(game.ghosts).filter(ghost -> ghost.tile().equals(game.player.tile())).findAny().get();
			new SequentialTransition( //
					afterSeconds(1, game::hideGhosts), //
					player3D.dyingAnimation(r2D.getGhostColor(killer.id), sounds), //
					afterSeconds(2, () -> gameController.stateTimer().expire()) //
			).play();
		}
		case GHOST_DYING -> {
			sounds.play(GameSounds.GHOST_EATEN);
		}
		case LEVEL_STARTING -> {
			maze3D.createWallsAndDoors(game.world, r2D.getMazeSideColor(game.mazeNumber),
					r2D.getMazeTopColor(game.mazeNumber));
			maze3D.createFood(game.world, r2D.getFoodColor(game.mazeNumber));
			maze3D.energizerAnimations().forEach(Animation::stop);
			levelCounter3D.update(game);
			var message = Env.message("level_starting", game.levelNumber);
			showFlashMessage(1, message);
			afterSeconds(3, () -> gameController.stateTimer().expire()).play();
		}
		case LEVEL_COMPLETE -> {
			sounds.stopAll();
			maze3D.energizerAnimations().forEach(Animation::stop);
			Stream.of(ghosts3D).forEach(Ghost3D::setNormalSkinColor);
			var message = Env.LEVEL_COMPLETE_TALK.next() + "\n\n" + Env.message("level_complete", game.levelNumber);
			new SequentialTransition( //
					pause(2), //
					maze3D.createMazeFlashingAnimation(game.numFlashes), //
					afterSeconds(1, () -> game.player.hide()), //
					afterSeconds(0.5, () -> showFlashMessage(2, message)), //
					afterSeconds(2, () -> gameController.stateTimer().expire())).play();
		}
		case GAME_OVER -> {
			sounds.stopAll();
			showFlashMessage(3, Env.GAME_OVER_TALK.next());
		}
		default -> {
		}
		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			maze3D.energizerAnimations().forEach(Animation::stop);
			bonus3D.setVisible(false);
		}
	}
}