/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.util.ResourceManager.fmtMessage;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.event.SoundEvent;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.GhostAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.PacAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.GhostAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.scene2d.BootScene;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.scene2d.HelpMenus;
import de.amr.games.pacman.ui.fx.scene2d.MsPacManCreditScene;
import de.amr.games.pacman.ui.fx.scene2d.MsPacManIntermissionScene1;
import de.amr.games.pacman.ui.fx.scene2d.MsPacManIntermissionScene2;
import de.amr.games.pacman.ui.fx.scene2d.MsPacManIntermissionScene3;
import de.amr.games.pacman.ui.fx.scene2d.MsPacManIntroScene;
import de.amr.games.pacman.ui.fx.scene2d.PacManCreditScene;
import de.amr.games.pacman.ui.fx.scene2d.PacManCutscene1;
import de.amr.games.pacman.ui.fx.scene2d.PacManCutscene2;
import de.amr.games.pacman.ui.fx.scene2d.PacManCutscene3;
import de.amr.games.pacman.ui.fx.scene2d.PacManIntroScene;
import de.amr.games.pacman.ui.fx.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.GameClock;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No dashboard, no picture-in-picture view.
 * 
 * @author Armin Reichert
 */
public class PacManGames2dUI implements PacManGamesUserInterface, GameEventListener {

	protected final Map<GameVariant, GameSceneConfiguration> gameSceneConfig = new EnumMap<>(GameVariant.class);
	protected GameClock clock;
	protected Theme theme;
	protected Stage stage;
	protected FlashMessageView flashMessageView = new FlashMessageView();
	protected HelpMenus helpMenus;
	protected GameController gameController;
	protected Pane mainSceneRoot;
	protected KeyboardSteering keyboardSteering;
	protected GameScene currentGameScene;
	private AudioClip currentVoice;
	private boolean canvasScaled;

	@Override
	public void init(Stage stage, Settings settings, Theme theme) {
		checkNotNull(stage);
		checkNotNull(settings);
		checkNotNull(theme);

		this.stage = stage;
		stage.setFullScreen(settings.fullScreen);
		this.theme = theme;
		this.gameController = new GameController(settings.variant);
		configureGameScenes();
		createMainScene(stage, settings);
		configureHelpMenus();
		configurePacSteering();
		configureBindings(settings);
		GameEvents.addListener(this);

		clock = new GameClock(this::onTick, this::onRender);
		clock.pausedPy.addListener((py, ov, nv) -> updateStage());
		clock.targetFrameratePy.set(GameModel.FPS);
	}

	protected void onTick() {
		gameController.update();
		if (currentGameScene != null) {
			currentGameScene.update();
		}
	}

	protected void onRender() {
		flashMessageView.update();
		if (currentGameScene != null) {
			currentGameScene.render();
		}
	}

	protected void configureGameScenes() {
		{
			//@formatter:off
			var config = new GameSceneConfiguration(
				new BootScene(),
				new MsPacManIntroScene(),
				new MsPacManCreditScene(),
				new PlayScene2D(),
				null,
				new MsPacManIntermissionScene1(),
				new MsPacManIntermissionScene2(),
				new MsPacManIntermissionScene3()
			);
			gameSceneConfig.put(GameVariant.MS_PACMAN, config);
		}

		{
			var config = new GameSceneConfiguration(
				new BootScene(),
				new PacManIntroScene(),
				new PacManCreditScene(),
				new PlayScene2D(),
				null,
				new PacManCutscene1(),
				new PacManCutscene2(),
				new PacManCutscene3()
			);
			gameSceneConfig.put(GameVariant.PACMAN, config);
	  	//@formatter:on
		}
	}

	protected void createMainScene(Stage stage, Settings settings) {
		mainSceneRoot = new StackPane();
		// Without this, there appears an ugly vertical line right of the embedded subscene
		mainSceneRoot.setBackground(ResourceManager.coloredBackground(theme.color("wallpaper.color")));
		mainSceneRoot.getChildren().add(new Text("(Game scene)"));
		mainSceneRoot.getChildren().add(flashMessageView);

		var mainScene = new Scene(mainSceneRoot, settings.zoom * 28 * 8, settings.zoom * 36 * 8, Color.BLACK);

		mainScene.setOnKeyPressed(this::handleKeyPressed);
		mainScene.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				resizeStageToFitCurrentGameScene();
			}
		});

		stage.setScene(mainScene);
	}

	protected void configureHelpMenus() {
		helpMenus = new HelpMenus(PacManGames2d.TEXTS);
		helpMenus.setFont(theme.font("font.monospaced", 12));
	}

	protected void resizeStageToFitCurrentGameScene() {
		if (currentGameScene != null && !currentGameScene.is3D() && !stage.isFullScreen()) {
			stage.setWidth(currentGameScene.sceneContainer().getWidth() + 16); // don't ask me why
		}
	}

	protected void configurePacSteering() {
		keyboardSteering = new KeyboardSteering();
		gameController.setManualPacSteering(keyboardSteering);
		// TODO: maybe only play scene should handle steering keys?
		stage.getScene().addEventHandler(KeyEvent.KEY_PRESSED, keyboardSteering);
	}

	@Override
	public void show() {
		stage.setMinWidth(241);
		stage.setMinHeight(328);
		stage.centerOnScreen();
		stage.requestFocus();
		stage.show();
		clock.start();
	}

	@Override
	public Theme theme() {
		return theme;
	}

	@Override
	public SpritesheetMsPacManGame spritesheetMsPacManGame() {
		return theme.get("mspacman.spritesheet");
	}

	@Override
	public SpritesheetPacManGame spritesheetPacManGame() {
		return theme.get("pacman.spritesheet");
	}

	protected void updateStage() {
		mainSceneRoot.setBackground(theme.background("wallpaper.background"));
		switch (gameVariant()) {
		case MS_PACMAN: {
			var messageKey = clock.pausedPy.get() ? "app.title.ms_pacman.paused" : "app.title.ms_pacman";
			stage.setTitle(ResourceManager.fmtMessage(PacManGames2d.TEXTS, messageKey, ""));
			stage.getIcons().setAll(theme.image("mspacman.icon"));
		}
			break;
		case PACMAN: {
			var messageKey = clock.pausedPy.get() ? "app.title.pacman.paused" : "app.title.pacman";
			stage.setTitle(ResourceManager.fmtMessage(PacManGames2d.TEXTS, messageKey, ""));
			stage.getIcons().setAll(theme.image("pacman.icon"));
		}
			break;
		default:
			throw new IllegalGameVariantException(gameVariant());
		}
	}

	/**
	 * @param settings application settings
	 */
	protected void configureBindings(Settings settings) {
		// snooze...
	}

	protected GameScene sceneMatchingCurrentGameState() {
		var config = gameSceneConfig.get(gameVariant());
		switch (gameState()) {
		case BOOT:
			return config.bootScene();
		case CREDIT:
			return config.creditScene();
		case INTRO:
			return config.introScene();
		case INTERMISSION:
			return config.cutScene(gameLevel().intermissionNumber);
		case INTERMISSION_TEST:
			return config.cutScene(game().intermissionTestNumber);
		default:
			return config.playScene();
		}
	}

	private GameLevel gameLevel() {
		return game().level().orElseThrow(IllegalStateException::new);
	}

	protected void updateGameScene(boolean reload) {
		var nextGameScene = sceneMatchingCurrentGameState();
		if (nextGameScene == null) {
			throw new IllegalStateException(String.format("No game scene found for game state %s.", gameState()));
		}
		if (reload || nextGameScene != currentGameScene) {
			changeGameScene(nextGameScene);
		}
		updateStage();
	}

	protected void changeGameScene(GameScene newGameScene) {
		var prevGameScene = currentGameScene;
		if (prevGameScene != null) {
			prevGameScene.end();
		}
		currentGameScene = newGameScene;
		currentGameScene.setParentScene(stage.getScene());
		// TODO check this
		if (currentGameScene instanceof GameScene2D) {
			var scene2D = (GameScene2D) currentGameScene;
			// This avoids a vertical line on the left side of the embedded 2D game scene
			var wallpaperColor = theme().color("wallpaper.color");
			scene2D.setWallpaperColor(wallpaperColor);
			scene2D.root()
					.setBackground(gameController.state() == GameState.BOOT ? ResourceManager.coloredBackground(Color.BLACK)
							: ResourceManager.coloredBackground(wallpaperColor));
			scene2D.setCanvasScaled(canvasScaled);
		}
		currentGameScene.setContext(new GameSceneContext(gameController, this));
		currentGameScene.init();
		mainSceneRoot.getChildren().set(0, currentGameScene.sceneContainer());
		Logger.trace("Game scene changed from {} to {}", prevGameScene, currentGameScene);
	}

	public void handleKeyPressed(KeyEvent keyEvent) {
		Keyboard.accept(keyEvent);
		handleKeyboardInput();
		currentGameScene.handleKeyboardInput();
		Keyboard.clearState();
	}

	protected void handleKeyboardInput() {
		if (Keyboard.pressed(PacManGames2d.KEY_SHOW_HELP)) {
			showHelp();
		} else if (Keyboard.pressed(PacManGames2d.KEY_AUTOPILOT)) {
			toggleAutopilot();
		} else if (Keyboard.pressed(PacManGames2d.KEY_BOOT)) {
			if (gameController().state() != GameState.BOOT) {
				reboot();
			}
		} else if (Keyboard.pressed(PacManGames2d.KEY_DEBUG_INFO)) {
			Ufx.toggle(PacManGames2d.PY_SHOW_DEBUG_INFO);
		} else if (Keyboard.pressed(PacManGames2d.KEY_IMMUNITIY)) {
			toggleImmunity();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PAUSE)) {
			togglePaused();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PAUSE_STEP) || Keyboard.pressed(PacManGames2d.KEY_SINGLE_STEP)) {
			oneSimulationStep();
		} else if (Keyboard.pressed(PacManGames2d.KEY_TEN_STEPS)) {
			tenSimulationSteps();
		} else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_FASTER)) {
			changeSimulationSpeed(5);
		} else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_SLOWER)) {
			changeSimulationSpeed(-5);
		} else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_NORMAL)) {
			resetSimulationSpeed();
		} else if (Keyboard.pressed(PacManGames2d.KEY_QUIT)) {
			restartIntro();
		} else if (Keyboard.pressed(PacManGames2d.KEY_TEST_LEVELS)) {
			startLevelTestMode();
		} else if (Keyboard.pressed(PacManGames2d.KEY_FULLSCREEN)) {
			stage.setFullScreen(true);
		} else if (Keyboard.pressed(PacManGames2d.KEY_CANVAS_SCALED)) {
			toggleCanvasScaled();
		}
	}

	@Override
	public void onGameEvent(GameEvent e) {
		Logger.trace("Event received: {}", e);
		// call event specific handler
		GameEventListener.super.onGameEvent(e);
		if (currentGameScene != null) {
			currentGameScene.onGameEvent(e);
		}
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		if (e.oldGameState == GameState.BOOT) {
			playVoice(theme.audioClip("voice.explain"), 1);
		}
		updateGameScene(false);
	}

	@Override
	public void onUnspecifiedChange(GameEvent e) {
		updateGameScene(true);
	}

	@Override
	public void onLevelBeforeStart(GameEvent e) {
		e.game.level().ifPresent(level -> {
			switch (level.game().variant()) {
			case MS_PACMAN: {
				var ss = spritesheetMsPacManGame();
				level.pac().setAnimations(new PacAnimationsMsPacManGame(level.pac(), ss));
				level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimationsMsPacManGame(ghost, ss)));
				break;
			}
			case PACMAN: {
				var ss = spritesheetPacManGame();
				level.pac().setAnimations(new PacAnimationsPacManGame(level.pac(), ss));
				level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimationsPacManGame(ghost, ss)));
				break;
			}
			default:
				throw new IllegalGameVariantException(level.game().variant());
			}
			Logger.info("Created creature and world animations for level #{}", level.number());
		});
		updateGameScene(true);
	}

	private String soundPrefix() {
		return gameVariant() == GameVariant.MS_PACMAN ? "mspacman." : "pacman.";
	}

	@Override
	public void onSoundEvent(SoundEvent event) {
		var p = soundPrefix();
		switch (event.id) {
		case GameModel.SE_BONUS_EATEN:
			theme.audioClip(p + "audio.bonus_eaten").play();
			break;
		case GameModel.SE_CREDIT_ADDED:
			theme.audioClip(p + "audio.credit").play();
			break;
		case GameModel.SE_EXTRA_LIFE:
			theme.audioClip(p + "audio.extra_life").play();
			break;
		case GameModel.SE_GHOST_EATEN:
			theme.audioClip(p + "audio.ghost_eaten").play();
			break;
		case GameModel.SE_HUNTING_PHASE_STARTED_0:
			ensureSirenStarted(0);
			break;
		case GameModel.SE_HUNTING_PHASE_STARTED_2:
			ensureSirenStarted(1);
			break;
		case GameModel.SE_HUNTING_PHASE_STARTED_4:
			ensureSirenStarted(2);
			break;
		case GameModel.SE_HUNTING_PHASE_STARTED_6:
			ensureSirenStarted(3);
			break;
		case GameModel.SE_READY_TO_PLAY:
			theme.audioClip(p + "audio.game_ready").play();
			break;
		case GameModel.SE_PACMAN_DEATH:
			theme.audioClip(p + "audio.pacman_death").play();
			break;
		case GameModel.SE_PACMAN_FOUND_FOOD:
			// TODO this does not sound as in the original game
			ensureLoop(theme.audioClip(p + "audio.pacman_munch"), AudioClip.INDEFINITE);
			break;
		case GameModel.SE_PACMAN_POWER_ENDS: {
			theme.audioClip(p + "audio.pacman_power").stop();
			event.game.level().ifPresent(level -> ensureSirenStarted(level.huntingPhase() / 2));
		}
			break;
		case GameModel.SE_PACMAN_POWER_STARTS: {
			stopSirens();
			theme.audioClip(p + "audio.pacman_power").stop();
			theme.audioClip(p + "audio.pacman_power").setCycleCount(AudioClip.INDEFINITE);
			theme.audioClip(p + "audio.pacman_power").play();
		}
			break;
		case GameModel.SE_START_INTERMISSION_1: {
			switch (event.game.variant()) {
			case MS_PACMAN:
				theme.audioClip(p + "audio.intermission.1").play();
				break;
			case PACMAN: {
				theme.audioClip(p + "audio.intermission").setCycleCount(2);
				theme.audioClip(p + "audio.intermission").play();
			}
				break;
			default:
				throw new IllegalGameVariantException(event.game.variant());
			}
			break;
		}
		case GameModel.SE_START_INTERMISSION_2: {
			switch (event.game.variant()) {
			case MS_PACMAN:
				theme.audioClip(p + "audio.intermission.2").play();
				break;
			case PACMAN:
				theme.audioClip(p + "audio.intermission").setCycleCount(1);
				theme.audioClip(p + "audio.intermission").play();
				break;
			default:
				throw new IllegalGameVariantException(event.game.variant());
			}
			break;
		}
		case GameModel.SE_START_INTERMISSION_3: {
			switch (event.game.variant()) {
			case MS_PACMAN:
				theme.audioClip(p + "audio.intermission.3").play();
				break;
			case PACMAN: {
				theme.audioClip(p + "audio.intermission").setCycleCount(2);
				theme.audioClip(p + "audio.intermission").play();
			}
				break;
			default:
				throw new IllegalGameVariantException(event.game.variant());
			}
			break;
		}
		case GameModel.SE_STOP_ALL_SOUNDS:
			stopAllSounds();
			break;
		default: {
			// ignore
		}
		}
	}

	@Override
	public void stopAllSounds() {
		theme.audioClips().forEach(AudioClip::stop);
	}

	@Override
	public void stopMunchingSound() {
		var p = soundPrefix();
		theme.audioClip(p + "audio.pacman_munch").stop();
	}

	@Override
	public void loopGhostReturningSound() {
		var p = soundPrefix();
		ensureLoop(theme.audioClip(p + "audio.ghost_returning"), AudioClip.INDEFINITE);
	}

	@Override
	public void playGameOverSound() {
		var p = soundPrefix();
		theme.audioClip(p + "audio.game_over").play();
	}

	@Override
	public void playLevelCompleteSound() {
		var p = soundPrefix();
		theme.audioClip(p + "audio.level_complete").play();
	}

	@Override
	public void stopGhostReturningSound() {
		var p = soundPrefix();
		theme.audioClip(p + "audio.ghost_returning").stop();
	}

	public void ensureLoop(AudioClip clip, int repetitions) {
		if (!clip.isPlaying()) {
			clip.setCycleCount(repetitions);
			clip.play();
		}
	}

	private void startSiren(int sirenIndex) {
		var p = soundPrefix();
		stopSirens();
		var clip = theme.audioClip(p + "audio.siren." + (sirenIndex + 1));
		clip.setCycleCount(AudioClip.INDEFINITE);
		clip.play();
	}

	private Stream<AudioClip> sirens(GameVariant variant) {
		var p = soundPrefix();
		return Stream.of(p + "audio.siren.1", p + "audio.siren.2", p + "audio.siren.3", p + "audio.siren.4")
				.map(key -> theme.audioClip(key));
	}

	/**
	 * @param sirenIndex index of siren (0..3)
	 */
	@Override
	public void ensureSirenStarted(int sirenIndex) {
		if (sirens(gameVariant()).noneMatch(AudioClip::isPlaying)) {
			startSiren(sirenIndex);
		}
	}

	public void stopSirens() {
		sirens(gameVariant()).forEach(AudioClip::stop);
	}

	public void showHelp() {
		if (currentGameScene instanceof GameScene2D) {
			var scene2D = (GameScene2D) currentGameScene;
			scene2D.showHelpMenu(helpMenus, Duration.seconds(2));
		}
	}

	public void showFlashMessage(String message, Object... args) {
		showFlashMessageSeconds(1, message, args);
	}

	public void showFlashMessageSeconds(double seconds, String message, Object... args) {
		flashMessageView.showMessage(String.format(message, args), seconds);
	}

	@Override
	public void playVoice(AudioClip clip, float delaySeconds) {
		if (currentVoice != null && currentVoice.isPlaying()) {
			return; // don't interrupt voice
		}
		currentVoice = clip;
		if (delaySeconds > 0) {
			Ufx.actionAfterSeconds(delaySeconds, currentVoice::play).play();
		} else {
			currentVoice.play();
		}
	}

	@Override
	public void stopVoice() {
		if (currentVoice != null) {
			currentVoice.stop();
		}
	}

	@Override
	public GameClock clock() {
		return clock;
	}

	@Override
	public GameController gameController() {
		return gameController;
	}

	@Override
	public GameScene currentGameScene() {
		return currentGameScene;
	}

	// Actions

	@Override
	public void startGame() {
		if (game().hasCredit()) {
			stopVoice();
			gameController.startPlaying();
		}
	}

	@Override
	public void startCutscenesTest() {
		gameController.startCutscenesTest(1);
		showFlashMessage("Cut scenes");
	}

	@Override
	public void restartIntro() {
		currentGameScene.end();
		GameEvents.setSoundEventsEnabled(true);
		if (game().isPlaying()) {
			game().changeCredit(-1);
		}
		gameController.restart(INTRO);
	}

	public void reboot() {
		if (currentGameScene != null) {
			currentGameScene.end();
		}
		gameController.restart(GameState.BOOT);
	}

	@Override
	public void addCredit() {
		GameEvents.setSoundEventsEnabled(true);
		gameController.addCredit();
	}

	@Override
	public void enterLevel(int newLevelNumber) {
		if (gameState() == GameState.CHANGING_TO_NEXT_LEVEL) {
			return;
		}
		game().level().ifPresent(level -> {
			if (newLevelNumber > level.number()) {
				for (int n = level.number(); n < newLevelNumber - 1; ++n) {
					game().nextLevel();
				}
				gameController.changeState(GameState.CHANGING_TO_NEXT_LEVEL);
			} else if (newLevelNumber < level.number()) {
				// not implemented
			}
		});
	}

	@Override
	public void togglePaused() {
		Ufx.toggle(clock.pausedPy);
		// TODO mute and unmute?
		if (clock.pausedPy.get()) {
			theme.audioClips().forEach(AudioClip::stop);
		}
	}

	public void oneSimulationStep() {
		if (clock.pausedPy.get()) {
			clock.executeSingleStep(true);
		}
	}

	public void tenSimulationSteps() {
		if (clock.pausedPy.get()) {
			clock.executeSteps(10, true);
		}
	}

	public void changeSimulationSpeed(int delta) {
		int newFramerate = clock.targetFrameratePy.get() + delta;
		if (newFramerate > 0) {
			clock.targetFrameratePy.set(newFramerate);
			showFlashMessageSeconds(0.75, String.format("%dHz", newFramerate));
		}
	}

	public void resetSimulationSpeed() {
		clock.targetFrameratePy.set(GameModel.FPS);
		showFlashMessageSeconds(0.75, String.format("%dHz", clock.targetFrameratePy.get()));
	}

	@Override
	public void selectNextGameVariant() {
		gameController.selectGameVariant(gameVariant().next());
	}

	@Override
	public void toggleAutopilot() {
		gameController.toggleAutoControlled();
		var auto = gameController.isAutoControlled();
		String message = fmtMessage(PacManGames2d.TEXTS, auto ? "autopilot_on" : "autopilot_off");
		showFlashMessage(message);
		playVoice(theme.audioClip(auto ? "voice.autopilot.on" : "voice.autopilot.off"));
	}

	@Override
	public void toggleImmunity() {
		game().setImmune(!game().isImmune());
		var immune = game().isImmune();
		String message = fmtMessage(PacManGames2d.TEXTS, immune ? "player_immunity_on" : "player_immunity_off");
		showFlashMessage(message);
		playVoice(theme.audioClip(immune ? "voice.immunity.on" : "voice.immunity.off"));
	}

	@Override
	public void toggleCanvasScaled() {
		canvasScaled = !canvasScaled;
		if (currentGameScene instanceof GameScene2D) {
			GameScene2D scene2D = (GameScene2D) currentGameScene;
			scene2D.setCanvasScaled(canvasScaled);
			showFlashMessage(canvasScaled ? "Canvas SCALED" : "Canvas UNSCALED");
		}
	}

	public void startLevelTestMode() {
		if (gameState() == GameState.INTRO) {
			gameController.restart(GameState.LEVEL_TEST);
			showFlashMessage("Level TEST MODE");
		}
	}

	@Override
	public void cheatAddLives() {
		int newLivesCount = game().lives() + 3;
		game().setLives(newLivesCount);
		showFlashMessage(fmtMessage(PacManGames2d.TEXTS, "cheat_add_lives", newLivesCount));
	}

	@Override
	public void cheatEatAllPellets() {
		gameController.cheatEatAllPellets();
	}

	@Override
	public void cheatEnterNextLevel() {
		gameController.cheatEnterNextLevel();
	}

	@Override
	public void cheatKillAllEatableGhosts() {
		gameController.cheatKillAllEatableGhosts();
	}
}