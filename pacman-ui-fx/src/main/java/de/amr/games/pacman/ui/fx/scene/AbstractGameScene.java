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
package de.amr.games.pacman.ui.fx.scene;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.Scene;
import javafx.scene.SubScene;

/**
 * Base class for all scenes (2D and 3D).
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameScene extends DefaultGameEventHandler {

	public GameController gameController;
	public GameModel game;
	public SubScene fxSubScene;
	public SoundManager sounds;
	public Rendering2D r2D;

	public void setContext(GameController gameController, GameModel game, Rendering2D r2d, SoundManager sounds) {
		this.gameController = gameController;
		this.game = game;
		this.r2D = r2d;
		this.sounds = sounds;
	}

	public abstract SubScene createSubScene(Scene parent);

	public SubScene getSubScene() {
		return fxSubScene;
	}

	public abstract void init();

	public abstract void update();

	public abstract boolean is3D();

	public void end() {
		log("Scene '%s' ended", getClass().getName());
	}
}