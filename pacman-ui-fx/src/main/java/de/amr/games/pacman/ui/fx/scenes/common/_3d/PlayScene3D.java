package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusActivatedEvent;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.BonusExpiredEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangedEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._3d.Bonus3D;
import de.amr.games.pacman.ui.fx.entities._3d.Ghost3D;
import de.amr.games.pacman.ui.fx.entities._3d.LevelCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.Maze3D;
import de.amr.games.pacman.ui.fx.entities._3d.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Impl;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlaySceneCameras.CameraType;
import de.amr.games.pacman.ui.fx.sound.PlaySceneSoundController;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * 3D scene displaying the maze and the game play for both, Pac-Man and Ms.
 * Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final String[] CONGRATS = { "Well done", "Congrats", "Awesome", "You did it", "You're the man*in",
			"WTF" };

	private final SubScene fxScene;
	private final PlaySceneCameras cams;

	private PlaySceneSoundController soundController;
	private PacManGameController gameController;

	private CoordinateSystem coordSystem;
	private Box ground;
	private Group tgMaze;
	private Group player;
	private Map<Ghost, Ghost3D> ghosts3D;
	private Maze3D maze;
	private List<Node> energizers;
	private List<Transition> energizerAnimations;
	private List<Node> pellets;
	private Bonus3D bonus3D;
	private ScoreNotReally3D score3D;
	private Group livesCounter3D;
	private LevelCounter3D levelCounter3D;

	public PlayScene3D(PlaySceneSoundController soundHandler) {
		this.soundController = soundHandler;
		fxScene = new SubScene(new Group(), 800, 600, true, SceneAntialiasing.BALANCED);
		cams = new PlaySceneCameras(fxScene);
		cams.select(CameraType.DYNAMIC);
	}

	private void buildSceneGraph(GameVariant gameVariant, GameLevel gameLevel) {

		maze = new Maze3D(gameLevel.world, Rendering2D_Assets.getMazeWallColor(gameVariant, gameLevel.mazeNumber));

		PhongMaterial foodMaterial = new PhongMaterial(
				Rendering2D_Assets.getFoodColor(gameVariant, gameLevel.mazeNumber));

		energizers = gameLevel.world.energizerTiles()//
				.map(tile -> createEnergizer(tile, foodMaterial))//
				.collect(Collectors.toList());

		pellets = gameLevel.world.tiles()//
				.filter(gameLevel.world::isFoodTile)//
				.filter(not(gameLevel.world::isEnergizerTile))//
				.map(tile -> createPellet(tile, foodMaterial)).collect(Collectors.toList());

		player = createPlayer3D();

		ghosts3D = game().ghosts().collect(
				Collectors.toMap(Function.identity(), ghost -> new Ghost3D(ghost, Rendering2D_Impl.get(gameVariant))));

		bonus3D = new Bonus3D(gameVariant, Rendering2D_Impl.get(gameVariant));

		score3D = new ScoreNotReally3D();

		livesCounter3D = createLivesCounter3D(new V2i(2, 1));

		levelCounter3D = new LevelCounter3D(Rendering2D_Impl.get(gameVariant));
		levelCounter3D.tileRight = new V2i(25, 1);
		levelCounter3D.update(game());

		tgMaze = new Group();
		tgMaze.getTransforms().add(new Translate(-14 * 8, -18 * 8));
		tgMaze.getChildren().addAll(score3D, livesCounter3D, levelCounter3D);
		tgMaze.getChildren().addAll(maze.getBricks());
		tgMaze.getChildren().addAll(energizers);
		tgMaze.getChildren().addAll(pellets);
		tgMaze.getChildren().addAll(player);
		tgMaze.getChildren().addAll(ghosts3D.values());
		tgMaze.getChildren().add(bonus3D.get());

		AmbientLight ambientLight = new AmbientLight();

		PointLight playerLight = new PointLight();
		playerLight.translateXProperty().bind(player.translateXProperty());
		playerLight.translateYProperty().bind(player.translateYProperty());
		playerLight.lightOnProperty().bind(player.visibleProperty());
		playerLight.setTranslateZ(-4);

		tgMaze.getChildren().addAll(ambientLight, playerLight);

		ground = new Box(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT, 0.1);
		PhongMaterial groundMaterial = new PhongMaterial(Color.rgb(0, 0, 51));
		ground.setMaterial(groundMaterial);
		ground.setTranslateX(-4);
		ground.setTranslateY(-4);
		ground.setTranslateZ(4);

		coordSystem = new CoordinateSystem(fxScene.getWidth());

		fxScene.setRoot(new Group(coordSystem.getNode(), ground, tgMaze));
		fxScene.setFill(Color.rgb(0, 0, 0));
	}

	private Sphere createPellet(V2i tile, PhongMaterial material) {
		double r = 1;
		Sphere s = new Sphere(r);
		s.setMaterial(material);
		s.setTranslateX(tile.x * TS);
		s.setTranslateY(tile.y * TS);
		s.setTranslateZ(1);
		s.setUserData(tile);
		return s;
	}

	private Node createEnergizer(V2i tile, PhongMaterial material) {
		Sphere s = createPellet(tile, material);
		s.setRadius(3);
		return s;
	}

	private Group createPlayer3D() {
		Group player = GianmarcosModel3D.IT.createPacMan();
		return player;
	}

	private Group createLivesCounter3D(V2i tilePosition) {
		Group livesCounter = new Group();
		for (int i = 0; i < 5; ++i) {
			V2i tile = tilePosition.plus(2 * i, 0);
			Group liveIndicator = GianmarcosModel3D.IT.createPacMan();
			liveIndicator.setTranslateX(tile.x * TS);
			liveIndicator.setTranslateY(tile.y * TS);
			liveIndicator.setTranslateZ(0);
			livesCounter.getChildren().add(liveIndicator);
		}
		return livesCounter;
	}

	@Override
	public PacManGameController getGameController() {
		return gameController;
	}

	@Override
	public void setGameController(PacManGameController gameController) {
		this.gameController = gameController;
		soundController.setGameController(gameController);
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.empty();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + hashCode();
	}

	@Override
	public void stretchTo(double width, double height) {
		// data binding does the job
	}

	@Override
	public SubScene get() {
		return fxScene;
	}

	@Override
	public Optional<PlaySceneCameras> cams() {
		return Optional.of(cams);
	}

	@Override
	public void start() {
		log("Game scene %s: start", this);
		buildSceneGraph(gameController.gameVariant(), game().currentLevel);
		if (gameController.isAttractMode()) {
			score3D.setHiscoreOnly(true);
			livesCounter3D.setVisible(false);
		} else {
			score3D.setHiscoreOnly(false);
			livesCounter3D.setVisible(true);
		}
	}

	@Override
	public void end() {
		log("Game scene %s: end", this);
	}

	@Override
	public void update() {
		score3D.update(game(), cams.selectedCamera());
		for (int i = 0; i < 5; ++i) {
			livesCounter3D.getChildren().get(i).setVisible(i < game().lives);
		}
		energizers.forEach(energizer -> {
			V2i tile = (V2i) energizer.getUserData();
			energizer.setVisible(!game().currentLevel.isFoodRemoved(tile));
		});
		pellets.forEach(pellet -> {
			V2i tile = (V2i) pellet.getUserData();
			pellet.setVisible(!game().currentLevel.isFoodRemoved(tile));
		});
		updatePlayer();
		ghosts3D.values().forEach(Ghost3D::update);
		bonus3D.update(game().bonus);
		cams.updateSelectedCamera(player);
		soundController.update();
	}

	private void updatePlayer() {
		Pac pac = game().player;
		player.setVisible(pac.visible);
		player.setTranslateX(pac.position.x);
		player.setTranslateY(pac.position.y);
		player.setRotationAxis(Rotate.Z_AXIS);
		player.setRotate(
				pac.dir == Direction.LEFT ? 0 : pac.dir == Direction.UP ? 90 : pac.dir == Direction.RIGHT ? 180 : 270);
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		soundController.onGameEvent(gameEvent);

		if (gameEvent instanceof PacManGameStateChangedEvent) {
			onGameStateChange((PacManGameStateChangedEvent) gameEvent);
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			gameController.getUI().showFlashMessage("Extra life!");
		}

		else if (gameEvent instanceof BonusActivatedEvent) {
			bonus3D.showSymbol(game().bonus);
		}

		else if (gameEvent instanceof BonusExpiredEvent) {
			bonus3D.hide();
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			bonus3D.showPoints(game().bonus);
		}
	}

	private void onGameStateChange(PacManGameStateChangedEvent event) {

		soundController.onGameStateChange(event.oldGameState, event.newGameState);

		// enter READY
		if (event.newGameState == PacManGameState.READY) {
		}

		// enter HUNTING
		if (event.newGameState == PacManGameState.HUNTING) {
			startEnergizerAnimations();
		}

		// exit HUNTING
		if (event.oldGameState == PacManGameState.HUNTING && event.newGameState != PacManGameState.GHOST_DYING) {
			stopEnergizerAnimations();
			bonus3D.hide();
		}

		// enter PACMAN_DYING
		if (event.newGameState == PacManGameState.PACMAN_DYING) {
			playAnimationPlayerDying();
		}

		// enter LEVEL_COMPLETE
		if (event.newGameState == PacManGameState.LEVEL_COMPLETE) {
			playAnimationLevelComplete();
		}

		// enter LEVEL_STARTING
		if (event.newGameState == PacManGameState.LEVEL_STARTING) {
			levelCounter3D.update(event.gameModel);
			playAnimationLevelStarting();
		}
	}

	private void startEnergizerAnimations() {
		energizerAnimations = new ArrayList<>();
		energizers.forEach(energizer -> {
			ScaleTransition pumping = new ScaleTransition(Duration.seconds(0.25), energizer);
			pumping.setAutoReverse(true);
			pumping.setCycleCount(Transition.INDEFINITE);
			pumping.setFromX(0);
			pumping.setFromY(0);
			pumping.setFromZ(0);
			pumping.setToX(1.1);
			pumping.setToY(1.1);
			pumping.setToZ(1.1);
			pumping.play();
			energizerAnimations.add(pumping);
		});
	}

	private void stopEnergizerAnimations() {
		energizers.forEach(energizer -> {
			energizer.setScaleX(1);
			energizer.setScaleY(1);
			energizer.setScaleZ(1);
		});
		energizerAnimations.forEach(Transition::stop);
		energizerAnimations.clear();
	}

	private void playAnimationPlayerDying() {
		PauseTransition phase1 = new PauseTransition(Duration.seconds(1));
		phase1.setOnFinished(e -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			soundController.sounds.play(PacManGameSound.PACMAN_DEATH);
		});

		ScaleTransition expand = new ScaleTransition(Duration.seconds(1), player);
		expand.setToX(1.5);
		expand.setToY(1.5);
		expand.setToZ(1.5);

		ScaleTransition shrink = new ScaleTransition(Duration.seconds(1.5), player);
		shrink.setToX(0.1);
		shrink.setToY(0.1);
		shrink.setToZ(0.1);

		SequentialTransition animation = new SequentialTransition(phase1, expand, shrink);
		animation.setOnFinished(e -> {
			player.setScaleX(1);
			player.setScaleY(1);
			player.setScaleZ(1);
			game().player.visible = false;
			gameController.stateTimer().forceExpiration();
		});

		animation.play();
	}

	private void playAnimationLevelComplete() {
		gameController.stateTimer().reset();

		String congrats = CONGRATS[new Random().nextInt(CONGRATS.length)];
		String message = String.format("%s!\n\nLevel %d complete.", congrats, game().currentLevelNumber);
		gameController.getUI().showFlashMessage(message, 2);

		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setOnFinished(e -> {
			game().player.visible = false;
			game().ghosts().forEach(ghost -> ghost.visible = false);
		});

		PauseTransition phase2 = new PauseTransition(Duration.seconds(2));

		SequentialTransition animation = new SequentialTransition(phase1, phase2);
		animation.setOnFinished(e -> {
			gameController.stateTimer().forceExpiration();
		});
		animation.play();
	}

	private void playAnimationLevelStarting() {
		gameController.stateTimer().reset();
		gameController.getUI().showFlashMessage("Entering Level " + gameController.game().currentLevelNumber);

		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setOnFinished(e -> {
			game().player.visible = true;
			game().ghosts().forEach(ghost -> ghost.visible = true);
		});

		PauseTransition phase2 = new PauseTransition(Duration.seconds(2));

		SequentialTransition animation = new SequentialTransition(phase1, phase2);
		animation.setOnFinished(e -> gameController.stateTimer().forceExpiration());
		animation.play();
	}
}