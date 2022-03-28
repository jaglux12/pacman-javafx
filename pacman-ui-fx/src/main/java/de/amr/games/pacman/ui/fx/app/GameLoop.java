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

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;

/**
 * Game loop with configurable frame rate.
 * 
 * @author Armin Reichert
 */
public class GameLoop {

	private final BooleanProperty $timeMeasured = new SimpleBooleanProperty(false);

	public Runnable update;
	public Runnable render;

	private Timeline tl;
	private int totalTicks;
	private int fps;
	private int targetFrameRate;
	private long fpsCountStartTime;
	private int frames;

	public GameLoop() {
		setTargetFrameRate(60);
	}

	public int getTargetFrameRate() {
		return targetFrameRate;
	}

	public void setTargetFrameRate(int fps) {
		targetFrameRate = fps;
		boolean restart = false;
		if (tl != null) {
			tl.stop();
			restart = true;
		}
		Duration frameDuration = Duration.millis(1000d / targetFrameRate);
		tl = new Timeline(targetFrameRate);
		tl.setCycleCount(Animation.INDEFINITE);
		tl.getKeyFrames().add(new KeyFrame(frameDuration, e -> runSingleStep(!Env.$paused.get())));
		if (restart) {
			tl.play();
		}
	}

	public int getTotalTicks() {
		return totalTicks;
	}

	public int getFPS() {
		return fps;
	}

	public void start() {
		tl.play();
		log("Game loop started. Target frame rate: %d", targetFrameRate);
	}

	public void stop() {
		tl.stop();
		log("Game loop stopped");
	}

	public void runSingleStep(boolean updateEnabled) {
		long now = System.nanoTime();
		if (updateEnabled) {
			runUpdate();
		}
		render.run();
		totalTicks++;
		++frames;
		if (now - fpsCountStartTime > 1e9) {
			fps = frames;
			frames = 0;
			fpsCountStartTime = now;
		}
	}

	private void runUpdate() {
		if ($timeMeasured.get()) {
			double start_ns = System.nanoTime();
			update.run();
			double duration_ns = System.nanoTime() - start_ns;
			log("Update took %f milliseconds", duration_ns / 1e6);
		} else {
			update.run();
		}
	}
}