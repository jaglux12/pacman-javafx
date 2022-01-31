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

import javafx.event.EventHandler;
import javafx.scene.Camera;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Base class of all camera controllers.
 * 
 * @author Armin Reichert
 */
public interface CameraController<T> extends EventHandler<KeyEvent> {

	void attachTo(Camera cam);

	Camera cam();

	boolean keysEnabled();

	void reset();

	void update(T target);

	@Override
	default void handle(KeyEvent e) {
		if (!keysEnabled()) {
			return;
		}
		if (e.isControlDown()) {
			switch (e.getCode()) {
			case DIGIT0:
				cam().setTranslateX(0);
				cam().setTranslateY(0);
				cam().setTranslateZ(-630);
				cam().setRotationAxis(Rotate.X_AXIS);
				cam().setRotate(0);
				cam().setRotationAxis(Rotate.Y_AXIS);
				cam().setRotate(0);
				cam().setRotationAxis(Rotate.Z_AXIS);
				cam().setRotate(0);
				break;
			case LEFT:
				cam().setTranslateX(cam().getTranslateX() + 10);
				break;
			case RIGHT:
				cam().setTranslateX(cam().getTranslateX() - 10);
				break;
			case UP:
				cam().setTranslateY(cam().getTranslateY() + 10);
				break;
			case DOWN:
				cam().setTranslateY(cam().getTranslateY() - 10);
				break;
			case PLUS:
				cam().setTranslateZ(cam().getTranslateZ() + 10);
				break;
			case MINUS:
				cam().setTranslateZ(cam().getTranslateZ() - 10);
				break;
			default:
				break;
			}
		}
		if (e.isShiftDown()) {
			switch (e.getCode()) {
			case DOWN:
				cam().setRotationAxis(Rotate.X_AXIS);
				cam().setRotate((360 + cam().getRotate() - 1) % 360);
				break;
			case UP:
				cam().setRotationAxis(Rotate.X_AXIS);
				cam().setRotate((cam().getRotate() + 1) % 360);
				break;
			case LEFT:
				cam().setRotationAxis(Rotate.Z_AXIS);
				cam().setRotate((360 + cam().getRotate() - 1) % 360);
				break;
			case RIGHT:
				cam().setRotationAxis(Rotate.Z_AXIS);
				cam().setRotate((360 + cam().getRotate() + 1) % 360);
				break;
			default:
				break;
			}
		}
	}

	default String info() {
		return String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", cam().getTranslateX(), cam().getTranslateY(),
				cam().getTranslateZ(), cam().getRotate());
	}
}