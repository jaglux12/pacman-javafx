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
import static de.amr.games.pacman.model.world.World.HTS;
import static de.amr.games.pacman.model.world.World.TS;
import static de.amr.games.pacman.model.world.World.t;
import static de.amr.games.pacman.ui.fx._3d.entity.Maze3D.NodeInfo.info;
import static de.amr.games.pacman.ui.fx.util.Animations.afterSeconds;
import static de.amr.games.pacman.ui.fx.util.Animations.pause;

import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.LevelCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.LivesCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D;
import de.amr.games.pacman.ui.fx._3d.entity.Player3D;
import de.amr.games.pacman.ui.fx._3d.entity.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import de.amr.games.pacman.ui.fx.scene.ScenesPacMan;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.CoordinateSystem;
import javafx.animation.SequentialTransition;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D extends AbstractGameScene {

	final PacManModel3D model3D;
	final EnumMap<Perspective, PlayScene3DCameraController> cams = new EnumMap<>(Perspective.class);
	final Image floorImage = new Image(getClass().getResource("/common/escher-texture.jpg").toString());
	final AmbientLight ambientLight = new AmbientLight(Color.GHOSTWHITE);
	final CoordinateSystem coordSystem = new CoordinateSystem(1000);

	SoundManager sounds;
	Group playground;
	Maze3D maze3D;
	Player3D player3D;
	Ghost3D[] ghosts3D;
	Bonus3D bonus3D;
	ScoreNotReally3D score3D;
	LevelCounter3D levelCounter3D;
	LivesCounter3D livesCounter3D;
	Rendering2D r2D;

	public PlayScene3D(PacManGameUI_JavaFX ui, PacManModel3D model3D) {
		super(ui);
		this.model3D = model3D;
		coordSystem.visibleProperty().bind(Env.$axesVisible);
		Env.$perspective.addListener(($1, $2, $3) -> camController().ifPresent(PlayScene3DCameraController::reset));
	}

	@Override
	public void createFXSubScene(Scene parentScene) {
		fxSubScene = new SubScene(new Group(), 400, 300, true, SceneAntialiasing.BALANCED);
		fxSubScene.widthProperty().bind(parentScene.widthProperty());
		fxSubScene.heightProperty().bind(parentScene.heightProperty());
		var cam = new PerspectiveCamera(true);
		fxSubScene.setCamera(cam);
		fxSubScene.addEventHandler(KeyEvent.KEY_PRESSED, e -> camController().ifPresent(cc -> cc.handle(e)));
		cams.clear();
		cams.put(Perspective.CAM_FOLLOWING_PLAYER, new Cam_FollowingPlayer(cam));
		cams.put(Perspective.CAM_NEAR_PLAYER, new Cam_NearPlayer(cam));
		cams.put(Perspective.CAM_TOTAL, new Cam_Total(cam));
	}

	@Override
	public void init(Scene parentScene) {
		super.init(parentScene);

		final int width = game.world.numCols() * TS;
		final int height = game.world.numRows() * TS;

		switch (gameController.gameVariant) {
		case MS_PACMAN:
			r2D = ScenesMsPacMan.RENDERING;
			sounds = ScenesMsPacMan.SOUNDS;
			break;
		case PACMAN:
			r2D = ScenesPacMan.RENDERING;
			sounds = ScenesPacMan.SOUNDS;
			break;
		default:
			throw new IllegalArgumentException("Illegal game variant: " + gameController.gameVariant);
		}

		maze3D = new Maze3D(width, height, floorImage);
		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		maze3D.$resolution.bind(Env.$mazeResolution);
		maze3D.$resolution.addListener((x, y, z) -> buildMaze(game.mazeNumber, false));
		buildMaze(game.mazeNumber, true);

		player3D = new Player3D(game.player, model3D.createPacMan());

		ghosts3D = game.ghosts().map(ghost -> new Ghost3D(ghost, model3D.createGhost(), model3D.createGhostEyes(), r2D))
				.toArray(Ghost3D[]::new);

		bonus3D = new Bonus3D(r2D);

		score3D = new ScoreNotReally3D(r2D.getScoreFont());
		// TODO: maybe this is not the best solution to keep the score display in plain view
		score3D.setRotationAxis(Rotate.X_AXIS);
		score3D.rotateProperty().bind(fxSubScene.getCamera().rotateProperty());

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.getTransforms().add(new Translate(TS, TS, -HTS));
		livesCounter3D.setVisible(!gameController.attractMode);

		levelCounter3D = new LevelCounter3D(r2D);
		levelCounter3D.setRightPosition(t(GameModel.TILES_X - 1), TS);
		levelCounter3D.init(game);

		playground = new Group();
		playground.getTransforms().add(new Translate(-0.5 * width, -0.5 * height)); // center at origin
		playground.getChildren().addAll(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		playground.getChildren().addAll(ghosts3D);

		fxSubScene.setRoot(new Group(ambientLight, playground, coordSystem));
		camController().ifPresent(PlayScene3DCameraController::reset);
	}

	@Override
	public void update() {
		maze3D.updateState(game);
		player3D.update();
		Stream.of(ghosts3D).forEach(Ghost3D::update);
		bonus3D.update(game.bonus);
		score3D.scoreOverwrite = gameController.attractMode ? "GAME OVER!" : null;
		score3D.update(game);
		livesCounter3D.setVisibleItems(game.player.lives);
		camController().ifPresent(camController -> camController.update(this));

		sounds.setMuted(gameController.attractMode); // TODO check this

		// Update food visibility and start animations and audio in case of switching between 2D and 3D scene
		// TODO: still incomplete
		if (gameController.currentStateID == GameState.HUNTING) {
			maze3D.foodNodes().forEach(foodNode -> {
				foodNode.setVisible(!game.isFoodEaten(info(foodNode).tile));
			});
			maze3D.startEnergizerAnimations();
			AudioClip munching = sounds.getClip(GameSounds.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (game.player.starvingTicks > 10) {
					sounds.stop(GameSounds.PACMAN_MUNCH);
				}
			}
		}
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public Optional<PlayScene3DCameraController> camController() {
		if (!cams.containsKey(Env.$perspective.get())) {
			return Optional.empty();
		}
		return Optional.of(cams.get(Env.$perspective.get()));
	}

	private void buildMaze(int mazeNumber, boolean withFood) {
		maze3D.buildWallsAndDoors(game.world, r2D.getMazeSideColor(mazeNumber), r2D.getMazeTopColor(mazeNumber));
		if (withFood) {
			maze3D.buildFood(game.world, r2D.getFoodColor(mazeNumber));
		}
		log("Built 3D maze (resolution=%d, wall height=%.2f)", maze3D.$resolution.get(), maze3D.$wallHeight.get());
	}

	@Override
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		if (e.scatterPhase > 0) {
			sounds.stop(GameSounds.SIRENS.get(e.scatterPhase - 1));
		}
		GameSounds siren = GameSounds.SIRENS.get(e.scatterPhase);
		if (!sounds.getClip(siren).isPlaying())
			sounds.loop(siren, Integer.MAX_VALUE);
	}

	@Override
	public void onPlayerGainsPower(GameEvent e) {
		sounds.loop(GameSounds.PACMAN_POWER, Integer.MAX_VALUE);
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED) || ghost3D.ghost.is(GhostState.LOCKED))
				.forEach(Ghost3D::setBlueSkinColor);
	}

	@Override
	public void onPlayerLosingPower(GameEvent e) {
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED)) //
				.forEach(ghost3D -> ghost3D.playFlashingAnimation());
	}

	@Override
	public void onPlayerLostPower(GameEvent e) {
		sounds.stop(GameSounds.PACMAN_POWER);
		Stream.of(ghosts3D).forEach(Ghost3D::setNormalSkinColor);
	}

	@Override
	public void onPlayerFoundFood(GameEvent e) {
		if (e.tile.isEmpty()) { // happens when using the "eat all pellets except energizers" cheat
			maze3D.foodNodes().filter(node -> !info(node).energizer).forEach(maze3D::hideFoodNode);
		} else {
			V2i tile = e.tile.get();
			maze3D.foodNodeAt(tile).ifPresent(maze3D::hideFoodNode);
			AudioClip munching = sounds.getClip(GameSounds.PACMAN_MUNCH);
			if (!munching.isPlaying()) {
				sounds.loop(GameSounds.PACMAN_MUNCH, Integer.MAX_VALUE);
			}
		}
	}

	@Override
	public void onBonusActivated(GameEvent e) {
		bonus3D.showSymbol(game.bonus);
	}

	@Override
	public void onBonusEaten(GameEvent e) {
		bonus3D.showPoints(game.bonus);
		sounds.play(GameSounds.BONUS_EATEN);
	}

	@Override
	public void onBonusExpired(GameEvent e) {
		bonus3D.hide();
	}

	@Override
	public void onExtraLife(GameEvent e) {
		ui.showFlashMessage(1.5, Env.message("extra_life"));
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
	public void onGhostLeavingHouse(GameEvent e) {
		ghosts3D[e.ghost.get().id].setNormalSkinColor();
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		sounds.setMuted(gameController.attractMode); // TODO check this

		// enter READY
		if (e.newGameState == GameState.READY) {
			maze3D.reset();
			player3D.reset();
			Stream.of(ghosts3D).forEach(Ghost3D::reset);
			sounds.stopAll();
			sounds.setMuted(gameController.attractMode);
			if (!gameController.gameRunning) {
				sounds.play(GameSounds.GAME_READY);
			}
		}

		// enter HUNTING
		else if (e.newGameState == GameState.HUNTING) {
			maze3D.playEnergizerAnimations();
		}

		// enter PACMAN_DYING
		else if (e.newGameState == GameState.PACMAN_DYING) {
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setNormalSkinColor());
			sounds.stopAll();
			new SequentialTransition( //
					afterSeconds(1, game::hideGhosts), //
					player3D.dyingAnimation(sounds), //
					afterSeconds(2, this::continueGame) //
			).play();
		}

		// enter GHOST_DYING
		else if (e.newGameState == GameState.GHOST_DYING) {
			sounds.play(GameSounds.GHOST_EATEN);
		}

		// enter LEVEL_STARTING
		else if (e.newGameState == GameState.LEVEL_STARTING) {
			buildMaze(game.mazeNumber, true);
			levelCounter3D.init(game);
			var message = Env.message("level_starting", game.levelNumber);
			ui.showFlashMessage(1, message);
			afterSeconds(3, this::continueGame).play();
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == GameState.LEVEL_COMPLETE) {
			sounds.stopAll();
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setNormalSkinColor());
			var message = Env.LEVEL_COMPLETE_TALK.next() + "\n\n" + Env.message("level_complete", game.levelNumber);
			var animation = new SequentialTransition( //
					pause(1), //
					maze3D.flashingAnimation(game.numFlashes), //
					afterSeconds(1, () -> game.player.hide()), //
					afterSeconds(1, () -> ui.showFlashMessage(2, message)) //
			);
			animation.setOnFinished(ae -> continueGame());
			animation.play();
		}

		// enter GAME_OVER
		else if (e.newGameState == GameState.GAME_OVER) {
			sounds.stopAll();
			ui.showFlashMessage(3, Env.GAME_OVER_TALK.next());
		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			maze3D.stopEnergizerAnimations();
			bonus3D.hide();
		}
	}
}