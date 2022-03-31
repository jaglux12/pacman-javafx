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
package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import java.io.IOException;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.shell.ManualPlayerControl;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is the entry point of the Pac-Man and Ms. Pac-Man games.
 * 
 * <p>
 * The application is structured according to the MVC (model-view-controller) design pattern. It creates the controller
 * (which in turn creates the model(s)) and the view (JavaFX UI). A game loop drives the controller which implements the
 * complete game logic. Game events from the controller are handled by the UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameAppFX extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	private Options options;
	private GameController gameController;
	private GameUI ui;

	@Override
	public void init() throws Exception {
		log("Initializing application");
		options = new Options(getParameters().getUnnamed());
		Env.$3D.set(options.use3DScenes);
		Env.$perspective.set(options.perspective);
		gameController = new GameController(options.gameVariant);
		GameLoop.get().update = () -> {
			gameController.updateState();
			ui.update();
		};
		GameLoop.get().render = () -> ui.render();
		log("Application initialized. Game variant: %s", gameController.gameVariant);
	}

	@Override
	public void start(Stage primaryStage) throws IOException {
		log("Starting application");
		gameController.setPlayerControl(new ManualPlayerControl(primaryStage));
		ui = new GameUI(gameController, primaryStage, options.windowHeight * 0.77, options.windowHeight);
		ui.show(options.fullscreen);
		GameLoop.get().start();
		log("Application started. Stage size w=%.0f h=%.0f, 3D: %s, camera perspective: %s", ui.stage.getWidth(),
				ui.stage.getHeight(), options.use3DScenes, options.perspective);
	}
}