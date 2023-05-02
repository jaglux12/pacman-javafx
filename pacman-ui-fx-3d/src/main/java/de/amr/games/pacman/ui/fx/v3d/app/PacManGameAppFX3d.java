/*
MIT License

Copyright (c) 2022 Armin Reichert

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
package de.amr.games.pacman.ui.fx.v3d.app;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.app.AppRes;
import de.amr.games.pacman.ui.fx.app.Settings;
import de.amr.games.pacman.ui.fx.scene.GameSceneChoice;
import de.amr.games.pacman.ui.fx.scene2d.BootScene;
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
import de.amr.games.pacman.ui.fx.v3d.scene.PlayScene3D;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * @author Armin Reichert
 */
public class PacManGameAppFX3d extends Application {

	private static List<GameSceneChoice> createPacManScenes(GameController gc) {
		return List.of(
		//@formatter:off
			new GameSceneChoice(new BootScene(gc)),
			new GameSceneChoice(new PacManIntroScene(gc)),
			new GameSceneChoice(new PacManCreditScene(gc)),
			new GameSceneChoice(new PlayScene2D(gc), new PlayScene3D(gc)),
			new GameSceneChoice(new PacManCutscene1(gc)), 
			new GameSceneChoice(new PacManCutscene2(gc)),
			new GameSceneChoice(new PacManCutscene3(gc))
		//@formatter:on
		);
	}

	private static List<GameSceneChoice> createMsPacManScenes(GameController gc) {
		return List.of(
		//@formatter:off
			new GameSceneChoice(new BootScene(gc)),
			new GameSceneChoice(new MsPacManIntroScene(gc)), 
			new GameSceneChoice(new MsPacManCreditScene(gc)),
			new GameSceneChoice(new PlayScene2D(gc), new PlayScene3D(gc)),
			new GameSceneChoice(new MsPacManIntermissionScene1(gc)), 
			new GameSceneChoice(new MsPacManIntermissionScene2(gc)),
			new GameSceneChoice(new MsPacManIntermissionScene3(gc))
		//@formatter:on
		);
	}

	public static void main(String[] args) {
		launch(args);
	}

	private GameUI3d gameUI;

	@Override
	public void init() throws Exception {
		AppRes.load();
		AppRes3d.load();
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		var settings = new Settings(getParameters() != null ? getParameters().getNamed() : Collections.emptyMap());
		var gameController = new GameController(settings.variant);
		gameUI = new GameUI3d(primaryStage, settings, gameController, createMsPacManScenes(gameController),
				createPacManScenes(gameController));
		gameUI.simulation().start();
		Logger.info("Game started. Target frame rate: {}", gameUI.simulation().targetFrameratePy.get());
	}

	@Override
	public void stop() throws Exception {
		gameUI.simulation().stop();
		Logger.info("Game stopped");
	}
}