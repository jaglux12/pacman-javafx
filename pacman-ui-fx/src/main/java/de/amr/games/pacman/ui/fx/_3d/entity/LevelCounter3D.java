/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Spritesheet_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Spritesheet_PacMan;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D level counter.
 * 
 * @author Armin Reichert
 */
public class LevelCounter3D extends Group {

	private final GameVariant gameVariant;
	private final V2d rightPosition;

	public LevelCounter3D(double x, double y, GameVariant gameVariant) {
		this.rightPosition = new V2d(x, y);
		this.gameVariant = gameVariant;
	}

	public void update(GameModel game) {
		getChildren().clear();
		double x = rightPosition.x, y = rightPosition.y;
		for (int i = 0; i < game.levelCounter.size(); ++i) {
			int symbol = game.levelCounter.symbol(i);
			var symbolImage = getBonusSymbolImage(symbol);
			Box cube = createSpinningCube(symbolImage, i % 2 == 0);
			cube.setTranslateX(x);
			cube.setTranslateY(y);
			cube.setTranslateZ(-HTS);
			getChildren().add(cube);
			x -= t(2);
		}
	}

	private Image getBonusSymbolImage(int symbol) {
		// TODO do not access specific spritesheet from here
		return switch (gameVariant) {
		case MS_PACMAN -> {
			var ss = Spritesheet_MsPacMan.get();
			yield ss.subImage(ss.getSymbolSprite(symbol));
		}
		case PACMAN -> {
			var ss = Spritesheet_PacMan.get();
			yield ss.subImage(ss.getSymbolSprite(symbol));
		}
		};
	}

	private Box createSpinningCube(Image symbol, boolean forward) {
		Box cube = new Box(TS, TS, TS);
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseMap(symbol);
		cube.setMaterial(material);
		RotateTransition spinning = new RotateTransition(Duration.seconds(6), cube);
		spinning.setAxis(Rotate.X_AXIS);
		spinning.setCycleCount(Animation.INDEFINITE);
		spinning.setByAngle(360);
		spinning.setRate(forward ? 1 : -1);
		spinning.play();
		return cube;
	}
}