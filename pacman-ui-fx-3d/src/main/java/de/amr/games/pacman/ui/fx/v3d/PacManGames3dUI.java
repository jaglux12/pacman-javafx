/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.Settings;
import de.amr.games.pacman.ui.fx.input.KeyboardSteering;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadePalette;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp.*;

/**
 * User interface for Pac-Man and Ms. Pac-Man games.
 * <p>
 * The <strong>play scene</strong> is available in a 2D and a 3D version. All others scenes are 2D only.
 * <p>
 * The picture-in-picture view shows the 2D version of the 3D play scene. It is activated/deactivated by pressing key F2.
 * Size and transparency can be controlled using the dashboard.
 * <p>
 * 
 * @author Armin Reichert
 */
public class PacManGames3dUI extends PacManGames2dUI implements ActionHandler3D {

	public PacManGames3dUI(Stage stage, Settings settings) {
		super(stage, settings);
		PY_3D_DRAW_MODE.addListener((py, ov, nv) -> updateStage());
		PY_3D_ENABLED.addListener((py, ov, nv) -> updateStage());
		gamePage().dashboard().sections().forEach(section -> section.init(this));
	}

	@Override
	protected void populateTheme() {
		super.populateTheme(); // loads resources from 2D module

		ResourceManager rm = this::getClass; // loads resources from this module
		theme.set("model3D.pacman", new Model3D(rm.url("model3D/pacman.obj")));
		theme.set("model3D.ghost",  new Model3D(rm.url("model3D/ghost.obj")));
		theme.set("model3D.pellet", new Model3D(rm.url("model3D/12206_Fruit_v1_L3.obj")));

		theme.set("model3D.wallpaper", rm.imageBackground("graphics/sea-wallpaper.jpg",
			BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
			BackgroundPosition.CENTER,
			new BackgroundSize(1, 1, true, true, false, true)
		));

		theme.set("model3D.wallpaper.night", rm.imageBackground("graphics/sea-wallpaper-night.jpg",
			BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
			BackgroundPosition.CENTER,
			new BackgroundSize(1, 1, true, true, false, true)
		));

		theme.set("image.armin1970",                 rm.image("graphics/armin.jpg"));
		theme.set("icon.play",                       rm.image("graphics/icons/play.png"));
		theme.set("icon.stop",                       rm.image("graphics/icons/stop.png"));
		theme.set("icon.step",                       rm.image("graphics/icons/step.png"));

		loadFloorTextures(rm,"hexagon", "knobs", "plastic", "wood");

		theme.set("ghost.0.color.normal.dress",      ArcadePalette.RED);
		theme.set("ghost.0.color.normal.eyeballs",   ArcadePalette.PALE);
		theme.set("ghost.0.color.normal.pupils",     ArcadePalette.BLUE);

		theme.set("ghost.1.color.normal.dress",      ArcadePalette.PINK);
		theme.set("ghost.1.color.normal.eyeballs",   ArcadePalette.PALE);
		theme.set("ghost.1.color.normal.pupils",     ArcadePalette.BLUE);

		theme.set("ghost.2.color.normal.dress",      ArcadePalette.CYAN);
		theme.set("ghost.2.color.normal.eyeballs",   ArcadePalette.PALE);
		theme.set("ghost.2.color.normal.pupils",     ArcadePalette.BLUE);

		theme.set("ghost.3.color.normal.dress",      ArcadePalette.ORANGE);
		theme.set("ghost.3.color.normal.eyeballs",   ArcadePalette.PALE);
		theme.set("ghost.3.color.normal.pupils",     ArcadePalette.BLUE);

		theme.set("ghost.color.frightened.dress",    ArcadePalette.BLUE);
		theme.set("ghost.color.frightened.eyeballs", ArcadePalette.ROSE);
		theme.set("ghost.color.frightened.pupils",   ArcadePalette.ROSE);

		theme.set("ghost.color.flashing.dress",      ArcadePalette.PALE);
		theme.set("ghost.color.flashing.eyeballs",   ArcadePalette.ROSE);
		theme.set("ghost.color.flashing.pupils",     ArcadePalette.RED);

		theme.addAllToArray("mspacman.maze.foodColor",
			Color.rgb(222, 222, 255),
			Color.rgb(255, 255, 0),
			Color.rgb(255, 0, 0),
			Color.rgb(222, 222, 255),
			Color.rgb(0, 255, 255),
			Color.rgb(222, 222, 255)
		);

		theme.addAllToArray("mspacman.maze.wallBaseColor",
			Color.rgb(255, 0, 0),
			Color.rgb(222, 222, 255),
			Color.rgb(222, 222, 255),
			Color.rgb(255, 183, 81),
			Color.rgb(255, 255, 0),
			Color.rgb(255, 0, 0)
		);

		theme.addAllToArray("mspacman.maze.wallTopColor",
			Color.rgb(255, 183, 174),
			Color.rgb(71, 183, 255),
			Color.rgb(222, 151, 81),
			Color.rgb(222, 151, 81),
			Color.rgb(222, 151, 81),
			Color.rgb(222, 151, 81)
		);

		theme.set("mspacman.color.head",           Color.rgb(255, 255, 0));
		theme.set("mspacman.color.palate",         Color.rgb(191, 79, 61));
		theme.set("mspacman.color.eyes",           Color.rgb(33, 33, 33));
		theme.set("mspacman.color.boobs",          Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
		theme.set("mspacman.color.hairbow",        Color.rgb(255, 0, 0));
		theme.set("mspacman.color.hairbow.pearls", Color.rgb(33, 33, 255));

		theme.set("mspacman.maze.doorColor",       Color.rgb(255, 183, 255));

		theme.set("pacman.maze.wallBaseColor",     Color.rgb(33, 33, 255).brighter());
		theme.set("pacman.maze.wallTopColor",      Color.rgb(33, 33, 255).darker());
		theme.set("pacman.maze.doorColor",         Color.rgb(252, 181, 255));

		theme.set("pacman.color.head",             Color.rgb(255, 255, 0));
		theme.set("pacman.color.palate",           Color.rgb(191, 79, 61));
		theme.set("pacman.color.eyes",             Color.rgb(33, 33, 33));
	}

	private void loadFloorTextures(ResourceManager rm, String... names) {
		var ext = "jpg";
		for (var name : names) {
			var texture = new PhongMaterial();
			texture.setBumpMap(rm.image("graphics/textures/%s-bump.%s".formatted(name, ext)));
			texture.setDiffuseMap(rm.image("graphics/textures/%s-diffuse.%s".formatted(name, ext)));
			texture.diffuseColorProperty().bind(PY_3D_FLOOR_COLOR);
			theme.set("texture." + name, texture);
		}
		theme.addAllToArray("texture.names", (Object[]) names);
	}


	@Override
	protected void addGameScenes() {
		super.addGameScenes();
		for (var gameVariant : GameVariant.values())
		{
			var playScene3D = new PlayScene3D();
			playScene3D.bindSize(mainScene.widthProperty(), mainScene.heightProperty());
			gameScenes.get(gameVariant).put("play3D", playScene3D);
		}
	}

	@Override
	protected GamePage3D createGamePage() {
		checkNotNull(mainScene);
		var page = new GamePage3D(this);
		page.setSize(mainScene.getWidth(), mainScene.getHeight());
		// register event handler for opening page context menu
		mainScene.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
			currentGameScene().ifPresent(gameScene -> {
				page.contextMenu().hide();
				if (e.getButton() == MouseButton.SECONDARY && isPlayScene(gameScene)) {
					page.contextMenu().rebuild(this, gameScene);
					page.contextMenu().show(mainScene.getRoot(), e.getScreenX(), e.getScreenY());
				}
			})
		);
		gameScenePy.addListener((py, ov, newGameScene) -> page.onGameSceneChanged(newGameScene));
		return page;
	}

	public boolean isPlayScene(GameScene gameScene) {
		var config = sceneConfig();
		return gameScene == config.get("play") || gameScene == config.get("play3D");
	}

	public GamePage3D gamePage() {
		return (GamePage3D) gamePage;
	}

	@Override
	public ActionHandler3D actionHandler() {
		return this;
	}

	@Override
	protected void configurePacSteering() {
		// Enable steering with unmodified and CONTROL + cursor key
		var steering = new KeyboardSteering();
		steering.define(Direction.UP,    KeyCode.UP,    KeyCombination.CONTROL_DOWN);
		steering.define(Direction.DOWN,  KeyCode.DOWN,  KeyCombination.CONTROL_DOWN);
		steering.define(Direction.LEFT,  KeyCode.LEFT,  KeyCombination.CONTROL_DOWN);
		steering.define(Direction.RIGHT, KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
		gameController().setManualPacSteering(steering);
	}

	@Override
	protected void updateStage() {
		var variantKey = gameVariant() == GameVariant.MS_PACMAN ? "mspacman" : "pacman";
		var titleKey = "app.title." + variantKey + (gameClock().isPaused() ? ".paused" : "");
		var dimension = message(PY_3D_ENABLED.get() ? "threeD" : "twoD");
		stage.setTitle(message(titleKey, dimension));
		stage.getIcons().setAll(theme.image(variantKey + ".icon"));
		gamePage().updateBackground();
	}

	@Override
	protected GameScene sceneMatchingCurrentGameState() {
		var gameScene = super.sceneMatchingCurrentGameState();
		if (PY_3D_ENABLED.get() && gameScene == sceneConfig().get("play")) {
			return sceneConfig().getOrDefault("play3D", gameScene);
		}
		return gameScene;
	}

	@Override
	public void toggle2D3D() {
		currentGameScene().ifPresent(gameScene -> {
			Ufx.toggle(PY_3D_ENABLED);
			if (isPlayScene(gameScene)) {
				updateOrReloadGameScene(true);
				gameScene.onSceneVariantSwitch();
			}
			gameController().update();
			showFlashMessage(message(PY_3D_ENABLED.get() ? "use_3D_scene" : "use_2D_scene"));
		});
	}

	@Override
	public void togglePipVisible() {
		Ufx.toggle(PY_PIP_ON);
		showFlashMessage(message(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
	}

	@Override
	public void selectNextPerspective() {
		PY_3D_PERSPECTIVE.set(PY_3D_PERSPECTIVE.get().next());
		showFlashMessage(message("camera_perspective", message(PY_3D_PERSPECTIVE.get().name())));
	}

	@Override
	public void selectPrevPerspective() {
		PY_3D_PERSPECTIVE.set(PY_3D_PERSPECTIVE.get().prev());
		showFlashMessage(message("camera_perspective", message(PY_3D_PERSPECTIVE.get().name())));
	}

	@Override
	public void toggleDrawMode() {
		PY_3D_DRAW_MODE.set(PY_3D_DRAW_MODE.get() == DrawMode.FILL ? DrawMode.LINE : DrawMode.FILL);
	}
}