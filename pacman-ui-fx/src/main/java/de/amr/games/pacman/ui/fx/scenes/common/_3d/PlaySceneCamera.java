package de.amr.games.pacman.ui.fx.scenes.common._3d;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;

public abstract class PlaySceneCamera extends PerspectiveCamera implements EventHandler<KeyEvent> {

	public PlaySceneCamera() {
		super(true);
		reset();
	}

	public abstract void reset();

	public abstract void follow(Node target);

	protected double lerp(double current, double target) {
		return current + (target - current) * 0.02;
	}
}