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

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.BonusState;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D bonus symbol.
 * 
 * @author Armin Reichert
 */
public class Bonus3D extends Box {

	private final RotateTransition rotation;

	public Bonus3D() {
		super(TS, TS, TS);
		rotation = new RotateTransition(Duration.INDEFINITE);
		rotation.setNode(this);
		rotation.setAxis(Rotate.X_AXIS);
		rotation.setByAngle(360);
		// Not sure if this is really needed:
		visibleProperty().addListener(($visible, oldValue, newValue) -> {
			if (newValue.booleanValue() == false) {
				rotation.stop();
			}
		});
		setTranslateZ(-HTS);
		setVisible(false);
	}

	public void update(Bonus bonus) {
		if (bonus.state() != BonusState.INACTIVE) {
			V2d position = bonus.position();
			setTranslateX(position.x + getWidth() / 2);
			setTranslateY(position.y + getHeight() / 2);
		}
	}

	public void showSymbol(Bonus bonus, Image texture) {
		showRotating(texture, 1.0, Animation.INDEFINITE, 1);
		setWidth(TS);
	}

	public void showPoints(Bonus bonus, Image texture) {
		showRotating(texture, 1.0, 5, 2);
		setWidth(bonus.value() >= 1000 ? TS * 1.25 : TS);
	}

	private void showRotating(Image texture, double seconds, int cycleCount, int rate) {
		var skin = new PhongMaterial(Color.WHITE);
		skin.setBumpMap(texture);
		skin.setDiffuseMap(texture);
		setMaterial(skin);
		rotation.setDuration(Duration.seconds(seconds));
		rotation.setCycleCount(cycleCount);
		rotation.setRate(rate);
		rotation.play();
		setVisible(true);
	}
}